package com.apex.gateway.infrastructure.web.server;

import com.apex.engine.sbe.*;
import com.apex.gateway.infrastructure.web.server.enums.SessionState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.agrona.concurrent.UnsafeBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class B3SessionHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(B3SessionHandler.class);

    private final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
    private final NegotiateDecoder negotiateDecoder = new NegotiateDecoder();
    private final EstablishDecoder establishDecoder = new EstablishDecoder();
    private final NewOrderDecoder newOrderDecoder = new NewOrderDecoder();

    private final UnsafeBuffer agronaBuffer = new UnsafeBuffer(ByteBuffer.allocate(0));

    private SessionState state = SessionState.WAITING_NEGOTIATE;

    private final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
    private final NegotiateResponseEncoder negotiateResponseEncoder = new NegotiateResponseEncoder();
    private final EstablishAckEncoder establishAckEncoder = new EstablishAckEncoder();

    private final UnsafeBuffer writeBuffer = new UnsafeBuffer(ByteBuffer.allocateDirect(128));

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf nettyBuffer = (ByteBuf) msg;

        try{
            ByteBuffer nioBuffer = nettyBuffer.nioBuffer();
            agronaBuffer.wrap(nioBuffer);

            int offset = 4;
            headerDecoder.wrap(agronaBuffer, offset);

            int templateId = headerDecoder.templateId();
            int payloadOffset = offset + headerDecoder.encodedLength();

            switch (templateId) {
                case 1:
                    if (state == SessionState.ESTABLISHED) {
                        handlerOrder(ctx, payloadOffset);
                    } else {
                        logger.error("Order rejected: Session not established");
                    }
                    break;
                case 2:
                    handleNegotiate(ctx, payloadOffset);
                    break;
                case 3:
                    handleEstablish(ctx, payloadOffset);
                default:
                    logger.error("Invalid message type: {}", templateId);
            }
        }catch (Exception e) {
            logger.error("Error processing message: ", e);
        }finally {
            nettyBuffer.release();
        }
    }

    private void handlerOrder(ChannelHandlerContext ctx, int payloadOffset) {
        //TODO
    }

    private void handleNegotiate(ChannelHandlerContext ctx, int payloadOffset) {
        negotiateDecoder.wrap(agronaBuffer, payloadOffset, headerDecoder.blockLength(), headerDecoder.version());
        long sessionId = negotiateDecoder.sessionId();

        int sbeLength = encodeNegotiateResponse(sessionId);
        sendSofhFrame(ctx, sbeLength);

        this.state = SessionState.WAITING_ESTABLISH;
    }

    private void handleEstablish(ChannelHandlerContext ctx, int payloadOffset) {
        establishDecoder.wrap(agronaBuffer, payloadOffset, headerDecoder.blockLength(), headerDecoder.version());
        long sessionId = establishDecoder.sessionId();

        logger.info("Establish received! Session ID: {}", sessionId);

        int sbeLength = encodeEstablishAck(sessionId);

        this.state = SessionState.ESTABLISHED;

        logger.info("Sending establish ack...");
    }

    private int encodeNegotiateResponse(long sessionId) {
        headerEncoder.wrap(agronaBuffer, 0)
                .blockLength(negotiateResponseEncoder.sbeBlockLength())
                .templateId(negotiateResponseEncoder.sbeTemplateId())
                .schemaId(negotiateResponseEncoder.sbeSchemaId())
                .version(negotiateResponseEncoder.sbeSchemaVersion());

        negotiateResponseEncoder.wrap(agronaBuffer, headerEncoder.encodedLength())
                .sessionId(sessionId)
                .negotiationStatus(NegotiationStatus.ACCEPTED);

        return headerEncoder.encodedLength() + negotiateResponseEncoder.encodedLength();
    }

    private int encodeEstablishAck(long sessionId) {
        headerEncoder.wrap(writeBuffer, 0)
                .blockLength(establishAckEncoder.sbeBlockLength())
                .templateId(establishAckEncoder.sbeTemplateId())
                .schemaId(establishAckEncoder.sbeSchemaId())
                .version(establishAckEncoder.sbeSchemaVersion());

        establishAckEncoder.wrap(writeBuffer, headerEncoder.encodedLength())
                .sessionId(sessionId)
                .nextSeqNo(1);

        return headerEncoder.encodedLength() + establishAckEncoder.encodedLength();
    }

    private void sendSofhFrame(ChannelHandlerContext ctx, int sbeLength) {
        int totalLength = 4 + sbeLength;
        ByteBuf nettyWriteBuffer = ctx.alloc().buffer(totalLength);

        nettyWriteBuffer.writeShortLE(totalLength);
        nettyWriteBuffer.writeShortLE(0xEB50);

        ByteBuffer nioBuffer = writeBuffer.byteBuffer();
        nioBuffer.limit(sbeLength);
        nettyWriteBuffer.writeBytes(nioBuffer);

        ctx.writeAndFlush(nettyWriteBuffer);
    }
}
