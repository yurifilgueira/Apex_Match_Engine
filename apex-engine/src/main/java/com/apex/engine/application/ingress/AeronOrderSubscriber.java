package com.apex.engine.application.ingress;

import com.apex.engine.application.services.OrderService;
import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class AeronOrderSubscriber {

    private final Logger logger = LoggerFactory.getLogger(AeronOrderSubscriber.class);
    private final Aeron aeron;
    private final OrderService orderService;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Subscription subscription;

    @Value("${apex-engine.aeron.channel:aeron:udp?endpoint=0.0.0.0:40456}")
    private String channel;

    @Value("${apex-engine.aeron.stream-id:10}")
    private int streamId;

    public AeronOrderSubscriber(Aeron aeron, OrderService orderService) {
        this.aeron = aeron;
        this.orderService = orderService;
    }

    @PostConstruct
    public void start() {
        // Passo 2: Criar a Subscription (O Ouvinte)
        subscription = aeron.addSubscription(channel, streamId);
        logger.info("Aeron Subscription added for channel: {} and streamId: {}", channel, streamId);

        // Passo 3: Disparar a Thread do Loop de Polling (Busy-Spin)
        new Thread(this::doPolling, "Aeron-Subscriber-Loop").start();
    }

    private void doPolling() {
        // Estratégia de espera para equilibrar CPU e Latência
        IdleStrategy idleStrategy = new BackoffIdleStrategy(10, 10, 1000, 100000);

        // Callback que recebe os bytes binários do Aeron
        FragmentHandler fragmentHandler = (buffer, offset, length, header) -> {
            try {
                // Entrega direta para o OrderService (Zero-Copy)
                orderService.registerOrder(buffer, offset, length);
            } catch (Exception e) {
                logger.error("Error processing order from Aeron", e);
            }
        };

        while (running.get()) {
            // Tenta ler até 10 mensagens por vez do Aeron
            int fragmentsRead = subscription.poll(fragmentHandler, 10);
            
            // Gerencia o consumo de CPU se não houver mensagens
            idleStrategy.idle(fragmentsRead);
        }
        
        logger.info("Aeron Polling thread stopped.");
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (subscription != null) {
            subscription.close();
        }
    }
}
