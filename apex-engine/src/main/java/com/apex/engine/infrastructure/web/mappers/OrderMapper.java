package com.apex.engine.infrastructure.web.mappers;

import com.apex.engine.domain.model.entities.Order;
import com.apex.engine.domain.model.enums.Side;
import com.apex.engine.infrastructure.web.dtos.OrderDTO;
import com.apex.engine.sbe.NewOrderDecoder;
import com.apex.engine.sbe.SideEnum;

public class OrderMapper {

    private static final int PRICE_SCALE = 2;

    public static void toEntity(OrderDTO dto, Order entity) {
        entity.setTicker(dto.ticker());
        entity.setPrice(dto.price().movePointRight(PRICE_SCALE).longValueExact());
        entity.setSide(dto.side());
        entity.setOriginalQuantity(dto.quantity());
        entity.setQuantity(dto.quantity());
        entity.setTimestamp(System.currentTimeMillis());
    }

    public static void toEntity(NewOrderDecoder decoder, Order entity) {
        entity.setTicker(decoder.ticker());
        entity.setPrice(decoder.price());
        entity.setSide(mapSide(decoder.side()));
        entity.setOriginalQuantity(decoder.quantity());
        entity.setQuantity(decoder.quantity());
        entity.setTimestamp(System.currentTimeMillis());
    }

    private static Side mapSide(SideEnum sbeSide) {
        return sbeSide == SideEnum.BID ? Side.BID : Side.ASK;
    }
}
