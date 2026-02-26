package com.apex.engine.domain.model.events.impl;

import com.apex.engine.domain.model.entities.Order;
import com.apex.engine.domain.model.events.Event;
import com.lmax.disruptor.EventFactory;

public class OrderEvent extends Event {

    public final static EventFactory EVENT_FACTORY = OrderEvent::new;

    public OrderEvent() {
    }

    public OrderEvent(int value) {
        super(value);
    }



}
