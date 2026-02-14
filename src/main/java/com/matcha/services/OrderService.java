package com.matcha.services;

import com.matcha.domain.Order;
import com.matcha.domain.OrderBook;
import com.matcha.oderBooks.OrderBookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OrderService {

    private final OrderBookRepository orderBookRepository;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderService() {
        this.orderBookRepository = OrderBookRepository.getInstance();
    }

    public void registerOrder(Order order) {

        order.setTimestamp(Instant.now());

        if (!orderBookRepository.containsKey(order.getTicker())) {
            OrderBook orderBook = new OrderBook();
            orderBook.setTicker(order.getTicker());
            orderBook.addOrder(order);

            orderBookRepository.put(order.getTicker(), orderBook);
        }
        else {
            orderBookRepository.get(order.getTicker()).addOrder(order);
        }

        // logger.info("Order Registered: " + order.getTicker());

    }
}