package com.linkedin.batch;

import org.springframework.batch.core.SkipListener;

public class CustomSkipListener implements SkipListener<Order, TrackedOrder> {

    @Override
    public void onSkipInRead(Throwable throwable) {

    }

    @Override
    public void onSkipInWrite(TrackedOrder trackedOrder, Throwable throwable) {

    }

    @Override
    public void onSkipInProcess(Order order, Throwable throwable) {
        System.out.println("Skipping processing of order with id: " + order.getOrderId());
    }
}
