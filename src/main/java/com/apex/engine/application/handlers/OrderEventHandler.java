package com.apex.engine.application.handlers;

import com.apex.engine.application.matching.Engine;
import com.apex.engine.domain.model.events.impl.OrderEvent;
import com.lmax.disruptor.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderEventHandler implements EventHandler<OrderEvent> {

    @Override
    public void onEvent(OrderEvent orderEvent, long sequence, boolean endOfBatch) {
        orderEvent.getOrderBook().addOrder(orderEvent.getMaker());
    }
}
