package com.linkedin.batch;

import org.springframework.batch.item.ItemProcessor;

import java.util.UUID;

public class TrackedOrderItemProcessor implements ItemProcessor<Order, TrackedOrder> {

    @Override
    public TrackedOrder process(Order order) throws Exception {
        System.out.println("Processing order with id: " + order.getOrderId());
        System.out.println("Processing with thread " + Thread.currentThread().getName());

        TrackedOrder trackedOrder = new TrackedOrder(order);
        trackedOrder.setTrackingNumber(this.getTrackingNumber());
        return trackedOrder;
    }

    private String getTrackingNumber() throws OrderProcessingException {
        if (Math.random() < .03) {
            throw new OrderProcessingException();
        }
        return UUID.randomUUID().toString();
    }
}
