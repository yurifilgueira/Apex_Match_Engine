package com.apex.engine.domain.model;

import com.apex.engine.domain.model.enums.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderBook {
    private String ticker;
    private final List<Order> bidSide;
    private final List<Order> askSide;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OrderBook() {
        bidSide = new ArrayList<>();
        askSide = new ArrayList<>();
    }

    public OrderBook(String ticker) {
        this.ticker = ticker;
        this.bidSide = new ArrayList<>();
        this.askSide = new ArrayList<>();
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public List<Order> getBidSide() {
        return Collections.unmodifiableList(bidSide);
    }

    public synchronized void addOrder(Order order) {
        if (order.getSide() == Side.ASK) {
            tryMatch(order, bidSide);
            if (order.getQuantity() > 0) {
                askSide.add(order);
                askSide.sort((o1, o2) -> Double.compare(o1.getPrice(), o2.getPrice()));
            }
        } else {
            tryMatch(order, askSide);

            if (order.getQuantity() > 0) {
                bidSide.add(order);
                bidSide.sort((o1, o2) -> Double.compare(o2.getPrice(), o1.getPrice()));
            }
        }
    }

    public List<Order> getAskSide() {
        return Collections.unmodifiableList(askSide);
    }

    public void tryMatch(Order order, List<Order> orders) {

        while (!orders.isEmpty() && order.getQuantity() > 0) {
            if (order.getSide() == Side.BID) {

                Order ask = orders.getFirst();

                if (ask.getPrice() <= order.getPrice()) {
                    //logger.info("Matched ask price " + ask.getPrice() + " for order " + order.getTicker());

                    if (ask.getQuantity() < order.getQuantity()) {
                        order.setQuantity(order.getQuantity() - ask.getQuantity());

                        orders.remove(ask);
                    }
                    else if (ask.getQuantity() > order.getQuantity()) {
                        ask.setQuantity(ask.getQuantity() - order.getQuantity());
                        order.setQuantity(0);
                    }
                    else {
                        orders.remove(ask);
                        order.setQuantity(0);
                    }
                }else {
                    break;
                }
            } else if (order.getSide() == Side.ASK) {

                Order bid = orders.getFirst();

                if (bid.getPrice() >= order.getPrice()) {
                    //logger.info("Matched bid price " + bid.getPrice() + " for order " + order.getTicker());

                    if (bid.getQuantity() < order.getQuantity()) {
                        order.setQuantity(order.getQuantity() - bid.getQuantity());

                        orders.remove(bid);
                    }
                    else if (bid.getQuantity() > order.getQuantity()) {
                        bid.setQuantity(bid.getQuantity() - order.getQuantity());
                        order.setQuantity(0);
                    }
                    else {
                        orders.remove(bid);
                        order.setQuantity(0);
                    }
                } else {
                    break;
                }
            }
        }
    }
}
