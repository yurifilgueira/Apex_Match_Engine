package com.apex.engine.infrastructure.persistence;

import com.apex.engine.domain.model.OrderBook;

import java.util.concurrent.ConcurrentHashMap;

public class OrderBookRepository {

    private static final OrderBookRepository instance = new OrderBookRepository();

    private OrderBookRepository() {
    }

    public static OrderBookRepository getInstance() {
        return instance;
    }

    private final ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, OrderBook> getOrderBooks() {
        return orderBooks;
    }

    public boolean containsKey(String ticker) {
        return orderBooks.containsKey(ticker);
    }

    public void put(String ticker, OrderBook orderBook) {
        orderBooks.put(ticker, orderBook);
    }

    public OrderBook get(String ticker) {
        return orderBooks.get(ticker);
    }
}
