package com.apex.engine.application.handlers;

import com.apex.engine.application.matching.Engine;
import com.apex.engine.domain.model.events.impl.OrderEvent;
import com.lmax.disruptor.EventHandler;

public class OrderEventHandler implements EventHandler<OrderEvent>{

    @Override
    public void onEvent(OrderEvent orderEvent, long sequence, boolean endOfBatch) {
        Engine.tryMatch(orderEvent.getMaker(), orderEvent.getTakers());
    }

}
