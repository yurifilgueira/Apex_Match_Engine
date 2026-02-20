package com.apex.engine.application.services;

import com.apex.engine.domain.model.Order;
import com.apex.engine.domain.model.OrderBook;
import com.apex.engine.infrastructure.persistence.OrderBookRepository;
import com.apex.engine.infrastructure.web.dtos.OrderDTO;
import com.apex.engine.infrastructure.web.mappers.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderBookRepository orderBookRepository;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderService() {
        this.orderBookRepository = OrderBookRepository.getInstance();
    }

    public void registerOrder(OrderDTO dto) {
        Order order = OrderMapper.toEntity(dto);

        order.setTimestamp(System.currentTimeMillis());

        OrderBook orderBook = orderBookRepository.getOrderBooks().computeIfAbsent(order.getTicker(), ticker -> {
            OrderBook newBook = new OrderBook();
            newBook.setTicker(ticker);
            return newBook;
        });

        orderBook.addOrder(order);

        // logger.info("Order Registered: " + order.getTicker());

    }
}