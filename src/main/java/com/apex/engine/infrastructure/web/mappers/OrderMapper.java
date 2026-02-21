package com.apex.engine.infrastructure.web.mappers;

import com.apex.engine.domain.model.Order;
import com.apex.engine.domain.model.enums.Side;
import com.apex.engine.v1.grpc.OrderDTO;

public class OrderMapper {

    private static final int PRICE_SCALE = 2;

    public static Order toEntity(OrderDTO dto) {
        Order entity = new Order();

        entity.setTicker(dto.getTicker());
        entity.setPrice(dto.getPriceScaled());
        entity.setTimestamp(System.currentTimeMillis());
        entity.setSide(Side.valueOf(dto.getSide().toString()));
        entity.setOriginalQuantity(dto.getQuantity());
        entity.setQuantity(dto.getQuantity());

        return entity;
    }

}
