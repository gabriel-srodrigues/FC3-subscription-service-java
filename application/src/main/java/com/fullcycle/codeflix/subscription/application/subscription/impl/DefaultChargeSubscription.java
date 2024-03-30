package com.fullcycle.codeflix.subscription.application.subscription.impl;

import com.fullcycle.codeflix.subscription.application.subscription.ChargeSubscription;
import com.fullcycle.codeflix.subscription.domain.AggregateRoot;
import com.fullcycle.codeflix.subscription.domain.Identifier;
import com.fullcycle.codeflix.subscription.domain.exceptions.DomainException;
import com.fullcycle.codeflix.subscription.domain.payment.BillingAddress;
import com.fullcycle.codeflix.subscription.domain.payment.Payment;
import com.fullcycle.codeflix.subscription.domain.payment.PaymentGateway;
import com.fullcycle.codeflix.subscription.domain.person.Address;
import com.fullcycle.codeflix.subscription.domain.plan.Plan;
import com.fullcycle.codeflix.subscription.domain.plan.PlanGateway;
import com.fullcycle.codeflix.subscription.domain.subscription.Subscription;
import com.fullcycle.codeflix.subscription.domain.subscription.SubscriptionGateway;
import com.fullcycle.codeflix.subscription.domain.subscription.SubscriptionId;
import com.fullcycle.codeflix.subscription.domain.subscription.status.SubscriptionStatus;
import com.fullcycle.codeflix.subscription.domain.account.Account;
import com.fullcycle.codeflix.subscription.domain.account.AccountGateway;
import com.fullcycle.codeflix.subscription.domain.account.AccountId;
import com.fullcycle.codeflix.subscription.domain.utils.IdUtils;
import com.fullcycle.codeflix.subscription.domain.validation.ValidationError;

import java.util.Objects;

public class DefaultChargeSubscription extends ChargeSubscription {

    private final PaymentGateway paymentGateway;
    private final PlanGateway planGateway;
    private final SubscriptionGateway subscriptionGateway;
    private final AccountGateway accountGateway;

    public DefaultChargeSubscription(
            final PaymentGateway paymentGateway, PlanGateway planGateway,
            final SubscriptionGateway subscriptionGateway,
            final AccountGateway accountGateway
    ) {
        this.paymentGateway = Objects.requireNonNull(paymentGateway);
        this.planGateway = Objects.requireNonNull(planGateway);
        this.subscriptionGateway = Objects.requireNonNull(subscriptionGateway);
        this.accountGateway = Objects.requireNonNull(accountGateway);
    }

    @Override
    public Output execute(final Input in) {
        final var userId = new AccountId(in.userId());
        final var subscriptionId = new SubscriptionId(in.subscriptionId());

        final var subscription =
                subscriptionGateway.subscriptionOfId(subscriptionId)
                        .filter(it -> it.accountId().equals(userId))
                        .orElseThrow(() -> notFound(Subscription.class, subscriptionId));

        final var aPlan = this.planGateway.planOfId(subscription.planId())
                .orElseThrow(() -> notFound(Plan.class, subscription.planId()));

        final var user = this.accountGateway.accountOfId(userId)
                .orElseThrow(() -> notFound(Account.class, userId));

        final var aPayment = this.newPaymentWith(in.paymentType(), aPlan.price().amount(), user.billingAddress());
        final var aTransaction = this.paymentGateway.processPayment(aPayment);

        subscription.renew(aPlan, aTransaction.transactionId());
        subscriptionGateway.save(subscription);

        return new StdOutput(subscription.id().value(), subscription.status());
    }

    private Payment newPaymentWith(final String paymentType, final Double price, final Address billingAddress) {
        return Payment.create(
                paymentType,
                price,
                IdUtils.uniqueId(),
                new BillingAddress(
                        billingAddress.zipcode(),
                        billingAddress.number(),
                        billingAddress.complement(),
                        billingAddress.country()
                )
        );
    }

    private DomainException notFound(Class<? extends AggregateRoot<?>> clazz, Identifier id) {
        return DomainException.with(new ValidationError("%s with id %s was not found".formatted(clazz.getCanonicalName(), id.value())));
    }

    record StdOutput(String subscriptionId, SubscriptionStatus subscriptionStatus) implements Output {

    }
}
