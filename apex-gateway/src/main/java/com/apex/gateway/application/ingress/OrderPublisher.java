package com.apex.gateway.application.ingress;

import com.apex.engine.sbe.MessageHeaderEncoder;
import com.apex.engine.sbe.NewOrderDecoder;
import com.apex.engine.sbe.NewOrderEncoder;
import com.apex.engine.sbe.SideEnum;
import com.apex.gateway.infrastructure.web.dto.OrderDTO;
import com.apex.gateway.infrastructure.web.dto.enums.Side;
import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.netty.handler.codec.quic.QuicPathEvent;
import jakarta.annotation.PostConstruct;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderPublisher {

    @Value( "${apex-engine.aeron.channel:aeron:udp?endpoint=0.0.0.0:40456}")
    private String aeronChannel;
    @Value("${apex-engine.aeron.stream-id:10}")
    private int aeronStreamId;

    private MediaDriver mediaDriver;
    private Aeron aeron;
    private Publication publication;

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[1024]);
    private final NewOrderEncoder encoder = new NewOrderEncoder();
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final IdleStrategy idleStrategy = new BusySpinIdleStrategy();

    @PostConstruct
    public void init() {
        this.mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context()
                .threadingMode(ThreadingMode.SHARED)
                .dirDeleteOnStart(true));
        this.aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(mediaDriver.aeronDirectoryName()));
        this.publication = aeron.addPublication(aeronChannel, aeronStreamId);
    }

    public void publish(OrderDTO dto) {

        int length = encode(dto);

        long result;
        while ((result = publication.offer(buffer, 0, length)) < 0L) {
            if (result == Publication.BACK_PRESSURED) {
                idleStrategy.idle();
            } else {
                throw new RuntimeException("Publication offer failed: " + result);
            }
        }

    }

    private int encode(OrderDTO dto) {
        headerEncoder.wrap(buffer, 0)
                .blockLength(encoder.sbeBlockLength())
                .templateId(encoder.sbeTemplateId())
                .schemaId(encoder.sbeSchemaId())
                .version(encoder.sbeSchemaVersion());

        encoder.wrap(buffer, headerEncoder.encodedLength());
        encoder.price(dto.price().longValue())
                .quantity(dto.quantity())
                .side(dto.side().name().equals(Side.BID.name()) ? SideEnum.BID : SideEnum.ASK);

        encoder.ticker(dto.ticker());

        return headerEncoder.encodedLength() + encoder.encodedLength();
    }
}