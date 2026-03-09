package com.apex.engine.domain.model.entities;

import com.apex.engine.application.matching.Engine;
import com.apex.engine.domain.model.enums.Side;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class OrderBook {
    private String ticker;
    private final Long2ObjectRBTreeMap<PriceLevel> bidSide;
    private final Long2ObjectRBTreeMap<PriceLevel> askSide;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OrderBook() {
        this.bidSide = new Long2ObjectRBTreeMap<>(Collections.reverseOrder());
        this.askSide = new Long2ObjectRBTreeMap<>();
    }

    public OrderBook(Long2ObjectRBTreeMap<PriceLevel> bidSide, Long2ObjectRBTreeMap<PriceLevel> askSide, String ticker) {
        this.bidSide = bidSide;
        this.askSide = askSide;
        this.ticker = ticker;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void addOrder(Order order) {
        tryMatchNewOrder(order);
    }

    private void tryMatchNewOrder(Order order) {
        var oppositeSide = order.getSide() == Side.ASK ? bidSide : askSide;

        Engine.tryMatch(order, oppositeSide);

        if (order.getQuantity() > 0) {
            addOrderToBookSide(order);
        }
    }

    private void addOrderToBookSide(Order order) {
        var sameSide = order.getSide() == Side.ASK ? askSide : bidSide;
        long price = order.getPrice();

        PriceLevel level = sameSide.get(price);
        if (level == null) {
            level = new PriceLevel();
            sameSide.put(price, level);
        }

        level.addOrder(order);
    }
}