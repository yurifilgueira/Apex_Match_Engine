package com.apex.engine.infrastructure.config;

import com.apex.engine.application.handlers.OrderEventHandler;
import com.apex.engine.domain.model.events.impl.OrderEvent;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;

@Configuration
public class DisruptorConfig {

    private final OrderEventHandler orderEventHandler;
    @Value( "${apex-engine.disruptor.ring-buffer-size:4096}")
    private int ringBufferSize;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DisruptorConfig(OrderEventHandler orderEventHandler) {
        this.orderEventHandler = orderEventHandler;
    }

    @Bean(destroyMethod = "shutdown")
    public Disruptor<OrderEvent> disruptor() {
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        WaitStrategy waitStrategy = new BusySpinWaitStrategy();

        logger.info("Creating Ring Buffer with size: {}...", ringBufferSize);

        Disruptor<OrderEvent> disruptor = new Disruptor<>(
                OrderEvent.EVENT_FACTORY,
                ringBufferSize,
                threadFactory,
                ProducerType.MULTI,
                waitStrategy
        );

        disruptor.handleEventsWith(orderEventHandler);
        disruptor.start();
        return disruptor;
    }

    @Bean
    public RingBuffer<OrderEvent> ringBuffer(Disruptor<OrderEvent> disruptor) {
        return disruptor.getRingBuffer();
    }
}
