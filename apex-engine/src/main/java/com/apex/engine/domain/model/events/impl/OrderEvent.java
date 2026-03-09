package com.apex.engine.domain.model.events.impl;

import com.apex.engine.domain.model.entities.Order;
import com.apex.engine.domain.model.entities.OrderBook;
import com.apex.engine.domain.model.events.Event;
import com.lmax.disruptor.EventFactory;

public class OrderEvent extends Event {

    private final Order maker;
    private OrderBook orderBook;
    public final static EventFactory<OrderEvent> EVENT_FACTORY = OrderEvent::new;

    public OrderEvent() {
        this.maker = new Order();
    }

    public Order getMaker() {
        return maker;
    }

    public OrderBook getOrderBook() {
        return orderBook;
    }

    public void setOrderBook(OrderBook orderBook) {
        this.orderBook = orderBook;
    }
}
