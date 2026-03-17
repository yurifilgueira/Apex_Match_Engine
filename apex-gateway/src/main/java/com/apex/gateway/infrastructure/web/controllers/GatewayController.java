package com.apex.gateway.infrastructure.web.controllers;


import com.apex.gateway.application.ingress.OrderPublisher;
import com.apex.gateway.infrastructure.web.dto.OrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(value = "/orders")
public class GatewayController {

    private final OrderPublisher publisher;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    public GatewayController(OrderPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> submit(@RequestBody OrderDTO orderDTO) {
        return Mono.fromRunnable(() -> publisher.publish(orderDTO))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(s -> {
                    counter.incrementAndGet();
                    if (counter.get() % 10000 == 0) {
                        logger.info("Published {} orders", counter.get());
                    }
                })
                .thenReturn(ResponseEntity.accepted().build());
    }

}
