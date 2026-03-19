package com.apex.gateway.infrastructure.web.server.decode;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

public class B3EntryPointDecoder extends LengthFieldBasedFrameDecoder {

    public B3EntryPointDecoder() {
        super(ByteOrder.LITTLE_ENDIAN, 1024, 0, 2, -2, 0, true);
    }
}
