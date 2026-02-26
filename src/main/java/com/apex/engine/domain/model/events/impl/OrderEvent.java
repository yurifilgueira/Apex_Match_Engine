package com.apex.engine.domain.model.events.impl;

import com.apex.engine.domain.model.entities.Order;
import com.apex.engine.domain.model.entities.PriceLevel;
import com.apex.engine.domain.model.events.Event;
import com.lmax.disruptor.EventFactory;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;

public class OrderEvent extends Event {

    private Order maker;
    private Long2ObjectRBTreeMap<PriceLevel> takers;
    public final static EventFactory EVENT_FACTORY = OrderEvent::new;

    public OrderEvent() {
    }

    public Order getMaker() {
        return maker;
    }

    public void setMaker(Order maker) {
        this.maker = maker;
    }

    public Long2ObjectRBTreeMap<PriceLevel> getTakers() {
        return takers;
    }

    public void setTakers(Long2ObjectRBTreeMap<PriceLevel> takers) {
        this.takers = takers;
    }
}
