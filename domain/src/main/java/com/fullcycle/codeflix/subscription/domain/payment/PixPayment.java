package com.fullcycle.codeflix.subscription.domain.payment;

import com.fullcycle.codeflix.subscription.domain.AssertionConcern;

public class PixPayment implements Payment, AssertionConcern {

    private final double amount;
    private final String orderId;
    private final BillingAddress address;

    public PixPayment(
            final Double amount,
            final String orderId,
            final BillingAddress billingAddress
    ) {
        this.amount = amount;
        this.orderId = orderId;
        this.address = billingAddress;
    }

    @Override
    public double amount() {
        return amount;
    }

    @Override
    public String orderId() {
        return orderId;
    }

    @Override
    public BillingAddress address() {
        return address;
    }
}
