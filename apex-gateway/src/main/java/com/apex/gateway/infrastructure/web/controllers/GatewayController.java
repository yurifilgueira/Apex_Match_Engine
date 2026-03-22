//package com.apex.gateway.infrastructure.web.controllers;
//
//
//import com.apex.gateway.application.ingress.OrderPublisher;
//import com.apex.gateway.infrastructure.web.dto.OrderDTO;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;
//
//@RestController
//@RequestMapping(value = "/orders")
//public class GatewayController {
//
//    private final OrderPublisher publisher;
//
//    public GatewayController(OrderPublisher publisher) {
//        this.publisher = publisher;
//    }
//
//    @PostMapping
//    public Mono<ResponseEntity<Void>> submit(@RequestBody OrderDTO orderDTO) {
//        return Mono.fromRunnable(() -> publisher.publish(orderDTO))
//                .subscribeOn(Schedulers.boundedElastic())
//                .thenReturn(ResponseEntity.accepted().build());
//    }
//
//}
