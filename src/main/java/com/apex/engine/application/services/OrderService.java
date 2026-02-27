package com.apex.engine.application.services;

import com.apex.engine.domain.model.entities.Order;
import com.apex.engine.domain.model.entities.OrderBook;
import com.apex.engine.domain.model.events.impl.OrderEvent;
import com.apex.engine.infrastructure.persistence.OrderBookRepository;
import com.apex.engine.infrastructure.web.dtos.OrderDTO;
import com.apex.engine.infrastructure.web.mappers.OrderMapper;
import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderBookRepository orderBookRepository;
    private final RingBuffer<OrderEvent> ringBuffer;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderService(RingBuffer<OrderEvent> ringBuffer) {
        this.orderBookRepository = OrderBookRepository.getInstance();
        this.ringBuffer = ringBuffer;
    }

    public void registerOrder(OrderDTO dto) {
        Order order = OrderMapper.toEntity(dto);

        order.setTimestamp(System.currentTimeMillis());

        long sequenceId = ringBuffer.next();
        OrderEvent orderEvent = ringBuffer.get(sequenceId);
        orderEvent.setMaker(order);

        OrderBook orderBook = orderBookRepository.getOrderBooks().computeIfAbsent(order.getTicker(), ticker -> {
            OrderBook newBook = new OrderBook();
            newBook.setTicker(ticker);
            return newBook;
        });

        orderEvent.setOrderBook(orderBook);

        ringBuffer.publish(sequenceId);

        // logger.info("Ring Buffer capacity: {}", ringBuffer.remainingCapacity());

        // logger.info("Order Registered: " + order.getTicker());

    }
}