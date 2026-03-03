package com.apex.engine.application.handlers;

import com.apex.engine.domain.model.events.impl.OrderEvent;
import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderEventHandler implements EventHandler<OrderEvent> {

    private final Logger logger = LoggerFactory.getLogger(OrderEventHandler.class);

    @Override
    public void onEvent(OrderEvent orderEvent, long sequence, boolean endOfBatch) {
        try {
            if (orderEvent.getOrderBook() != null && orderEvent.getMaker() != null) {
                orderEvent.getOrderBook().addOrder(orderEvent.getMaker());
            } else {
                logger.error("Invalid event at sequence {}: OrderBook or Maker is null", sequence);
            }
        } catch (Exception e) {
            logger.error("Error adding order to order book", e);
        }
    }
}
