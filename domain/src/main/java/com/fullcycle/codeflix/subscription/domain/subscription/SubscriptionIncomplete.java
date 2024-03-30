package com.fullcycle.codeflix.subscription.domain.subscription;

import com.fullcycle.codeflix.subscription.domain.account.AccountId;
import com.fullcycle.codeflix.subscription.domain.utils.InstantUtils;

import java.time.Instant;
import java.time.LocalDate;

public record SubscriptionIncomplete(
        SubscriptionId subscriptionId,
        AccountId accountId,
        String reason,
        LocalDate dueDate,
        Instant occurredOn
) implements SubscriptionEvent {

    public SubscriptionIncomplete(final Subscription subscription, final String reason) {
        this(
                subscription.id(),
                subscription.accountId(),
                reason,
                subscription.dueDate(),
                InstantUtils.now()
        );
    }
}