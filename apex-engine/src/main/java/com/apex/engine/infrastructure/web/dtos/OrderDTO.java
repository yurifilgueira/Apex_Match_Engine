package com.apex.engine.infrastructure.web.dtos;

import com.apex.engine.domain.model.enums.Side;

import java.math.BigDecimal;

public record OrderDTO(
        String ticker,
        BigDecimal price,
        Side side,
        Integer quantity
) {
}
