package com.apex.engine.application.services;

import com.apex.engine.domain.model.entities.Order;
import com.apex.engine.domain.model.entities.OrderBook;
import com.apex.engine.domain.model.events.impl.OrderEvent;
import com.apex.engine.infrastructure.persistence.OrderBookRepository;
import com.apex.engine.infrastructure.web.mappers.OrderMapper;
import com.apex.engine.sbe.MessageHeaderDecoder;
import com.apex.engine.sbe.NewOrderDecoder;
import com.lmax.disruptor.RingBuffer;
import org.agrona.DirectBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderBookRepository orderBookRepository;
    private final RingBuffer<OrderEvent> ringBuffer;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private static final ThreadLocal<MessageHeaderDecoder> headerDecoderTl = ThreadLocal.withInitial(MessageHeaderDecoder::new);
    private static final ThreadLocal<NewOrderDecoder> dataDecoderTl = ThreadLocal.withInitial(NewOrderDecoder::new);

    public OrderService(RingBuffer<OrderEvent> ringBuffer) {
        this.orderBookRepository = OrderBookRepository.getInstance();
        this.ringBuffer = ringBuffer;
    }

    public void registerOrder(DirectBuffer buffer, int offset, int length) {
        MessageHeaderDecoder headerDecoder = headerDecoderTl.get();
        NewOrderDecoder dataDecoder = dataDecoderTl.get();

        dataDecoder.wrapAndApplyHeader(buffer, offset, headerDecoder);

        long sequenceId = ringBuffer.next();
        OrderEvent orderEvent = ringBuffer.get(sequenceId);

        Order order = orderEvent.getMaker();
        OrderMapper.toEntity(dataDecoder, order);

        OrderBook orderBook = orderBookRepository.getOrderBooks().computeIfAbsent(order.getTicker(), ticker -> {
            OrderBook newBook = new OrderBook();
            newBook.setTicker(ticker);
            return newBook;
        });

        orderEvent.setOrderBook(orderBook);

        ringBuffer.publish(sequenceId);
    }
}
