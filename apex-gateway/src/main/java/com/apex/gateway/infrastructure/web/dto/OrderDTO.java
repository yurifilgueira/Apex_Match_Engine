package com.apex.gateway.infrastructure.web.dto;


import com.apex.gateway.infrastructure.web.dto.enums.Side;

import java.math.BigDecimal;

public record OrderDTO(
        String ticker,
        BigDecimal price,
        Side side,
        Integer quantity
) {
}
