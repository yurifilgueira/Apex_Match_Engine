package com.apex.gateway.infrastructure.web.server;

import com.apex.engine.sbe.EstablishDecoder;
import com.apex.engine.sbe.MessageHeaderDecoder;
import com.apex.engine.sbe.NegotiateDecoder;
import com.apex.engine.sbe.NewOrderDecoder;
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
    private final SessionState state = SessionState.WAITING_NEGOTIATE;

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
        //TODO
    }

    private void handleEstablish(ChannelHandlerContext ctx, int payloadOffset) {
        //TODO
    }

}
