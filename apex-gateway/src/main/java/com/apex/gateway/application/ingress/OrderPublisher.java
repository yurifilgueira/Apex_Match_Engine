package com.apex.gateway.application.ingress;

import com.apex.engine.sbe.MessageHeaderEncoder;
import com.apex.engine.sbe.NewOrderEncoder;
import com.apex.engine.sbe.SideEnum;
import com.apex.gateway.infrastructure.web.dto.OrderDTO;
import com.apex.gateway.infrastructure.web.dto.enums.Side;
import io.aeron.Aeron;
import io.aeron.Publication;
import jakarta.annotation.PostConstruct;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.springframework.beans.factory.annotation.Value;

public class OrderPublisher {

    @Value("${apex-engine.aeron.channel:aeron:ipc}")
    private String aeronChannel;

    @Value("${apex-engine.aeron.stream-id:10}")
    private int aeronStreamId;

    @Value("${apex-engine.aeron.directory-name:/dev/shm/aeron}")
    private String aeronDirectoryName;

    private Aeron aeron;
    private Publication publication;

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[1024]);
    private final NewOrderEncoder encoder = new NewOrderEncoder();
    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final IdleStrategy idleStrategy = new BusySpinIdleStrategy();

    @PostConstruct
    public void init() {
        Aeron.Context ctx = new Aeron.Context()
                .aeronDirectoryName(aeronDirectoryName);

        this.aeron = Aeron.connect(ctx);
        this.publication = aeron.addPublication(aeronChannel, aeronStreamId);
    }

    public void publish(OrderDTO dto) {
        int length = encode(dto);

        long result;
        while ((result = publication.offer(buffer, 0, length)) < 0L) {
            if (result == Publication.BACK_PRESSURED || result == Publication.NOT_CONNECTED) {
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
