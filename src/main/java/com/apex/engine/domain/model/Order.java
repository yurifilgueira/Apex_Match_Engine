package com.apex.engine.domain.model;

import com.apex.engine.domain.model.enums.Side;

import java.time.Instant;

public class Order {
    private String ticker;
    private Double price;
    private Instant timestamp;
    private Side side;
    private Integer quantity;

    public Order() {
    }

    public Order(String ticker, Double price, Instant timestamp, Side side, Integer quantity) {
        this.ticker = ticker;
        this.price = price;
        this.timestamp = timestamp;
        this.side = side;
        this.quantity = quantity;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}