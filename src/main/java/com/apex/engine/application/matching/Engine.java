package com.apex.engine.application.matching;

import com.apex.engine.domain.model.Order;
import com.apex.engine.domain.model.PriceLevel;
import com.apex.engine.domain.model.enums.Side;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Engine {

    private static final Logger logger = LoggerFactory.getLogger(Engine.class);

    public static void tryMatch(Order maker, Long2ObjectRBTreeMap<PriceLevel> takers) {

        while (!takers.isEmpty() && maker.getQuantity() > 0) {
            long bestPrice = takers.firstLongKey();
            PriceLevel level = takers.get(bestPrice);

            if (level.hasNoOrders()) {
                takers.remove(bestPrice);
                continue;
            }

            if (maker.getSide() == Side.BID && maker.getPrice() < bestPrice) {
                break;
            } else if (maker.getSide() == Side.ASK && maker.getPrice() > bestPrice) {
                break;
            }

            Order taker = level.getFirst();

            int tradedQuantity = Math.min(maker.getQuantity(), taker.getQuantity());

//            logger.info("MATCH EXECUTED: Ticker={} Price={} Qty={} (Maker Side: {}, Taker Side: {})",
//                    maker.getTicker(), bestPrice, tradedQuantity, maker.getSide(), taker.getSide());

            maker.setQuantity(maker.getQuantity() - tradedQuantity);
            taker.setQuantity(taker.getQuantity() - tradedQuantity);

            if (taker.getQuantity() == 0) {
                level.removeFirst();

                if (level.hasNoOrders()) {
                    takers.remove(bestPrice);
                }
            }
        }
    }
}