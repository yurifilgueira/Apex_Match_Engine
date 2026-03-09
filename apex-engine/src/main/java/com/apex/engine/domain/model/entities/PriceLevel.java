package com.apex.engine.domain.model.entities;

import java.util.LinkedList;

public class PriceLevel {
    private long price;
    private int quantityOrders;
    private final LinkedList<Order> orders;

    public PriceLevel() {
        this.orders = new LinkedList<>();
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getQuantityOrders() {
        return quantityOrders;
    }

    public void addOrder(Order order) {
        orders.addLast(order);
        quantityOrders++;
    }

    public void removeFirst() {
        orders.removeFirst();
        quantityOrders--;
    }

    public Order getFirst() {
        return orders.getFirst();
    }

    public boolean hasNoOrders() {
        return orders.isEmpty();
    }
}