package com.apex.engine.infrastructure.web.mappers;

import com.apex.engine.domain.model.entities.Order;
import com.apex.engine.infrastructure.web.dtos.OrderDTO;

public class OrderMapper {

    private static final int PRICE_SCALE = 2;

    public static Order toEntity(OrderDTO dto, Order entity) {

        entity.setTicker(dto.ticker());
        entity.setPrice(dto.price().movePointRight(PRICE_SCALE).longValueExact());
        entity.setTimestamp(System.currentTimeMillis());
        entity.setSide(dto.side());
        entity.setOriginalQuantity(dto.quantity());
        entity.setQuantity(dto.quantity());

        entity.setTimestamp(System.currentTimeMillis());

        return entity;
    }

}
