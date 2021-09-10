package com.linkedin.batch;

import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class FreeShippingItemProcessor implements ItemProcessor<TrackedOrder, TrackedOrder> {

    @Override
    public TrackedOrder process(TrackedOrder trackedOrder) throws Exception {
        trackedOrder.setFreeShipping(trackedOrder.getCost().compareTo(new BigDecimal("80")) > 0);

       return trackedOrder.isFreeShipping() ? trackedOrder : null;
    }
}
