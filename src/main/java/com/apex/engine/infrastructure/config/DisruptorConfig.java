package com.apex.engine.infrastructure.config;

import com.apex.engine.application.handlers.OrderEventHandler;
import com.apex.engine.domain.model.events.impl.OrderEvent;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;

@Configuration
public class DisruptorConfig {

    private final OrderEventHandler orderEventHandler;

    public DisruptorConfig(OrderEventHandler orderEventHandler) {
        this.orderEventHandler = orderEventHandler;
    }

    @Bean(destroyMethod = "shutdown")
    public Disruptor<OrderEvent> disruptor() {
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        WaitStrategy waitStrategy = new BusySpinWaitStrategy();

        Disruptor<OrderEvent> disruptor = new Disruptor<>(
                OrderEvent.EVENT_FACTORY,
                16384,
                threadFactory,
                ProducerType.SINGLE,
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
