package com.apex.engine.infrastructure.web.controllers;

import com.apex.engine.application.services.OrderService;
import com.apex.engine.infrastructure.web.dtos.OrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/orders")
public class OrderController {

    private final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = "application/octet-stream")
    public ResponseEntity<?> createOrder(@RequestBody byte[] bytes) {
        //orderService.registerOrder(bytes);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok().build();
    }

}