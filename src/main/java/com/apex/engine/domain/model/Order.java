package com.apex.engine.domain.model;

import com.apex.engine.domain.model.enums.Side;

import java.time.Instant;

public class Order {
    private String ticker;
    private long price;
    private long timestamp;
    private Side side;
    private int quantity;
    private int originalQuantity;

    public Order() {
    }

    public Order(String ticker, long price, long timestamp, Side side, int quantity, int originalQuantity) {
        this.ticker = ticker;
        this.price = price;
        this.timestamp = timestamp;
        this.side = side;
        this.quantity = quantity;
        this.originalQuantity = originalQuantity;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public void setOriginalQuantity(int originalQuantity) {
        this.originalQuantity = originalQuantity;
    }
}