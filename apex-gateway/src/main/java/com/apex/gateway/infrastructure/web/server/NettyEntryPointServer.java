package com.apex.gateway.infrastructure.web.server;

import com.apex.gateway.infrastructure.web.server.decode.B3EntryPointDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class NettyEntryPointServer {

    @Value("${apex-engine.server.netty.entrypoint.port:7890}")
    private int port;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void start() {
        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());

        new Thread(() -> {
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast(new B3EntryPointDecoder());
                                ch.pipeline().addLast(new SimpleLoggerHandler());
                                ch.pipeline().addLast(new B3SessionHandler());
                            }
                        });

                logger.info("Starting Netty server on port {}...", port);

                ChannelFuture future = serverBootstrap.bind(port).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                logger.error("Error starting Netty server", e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }, "Netty-Server-Thread").start();
    }

    private class SimpleLoggerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf buf = (ByteBuf) msg;
            logger.info("TCP frame caught size: {} bytes | Hex: {}",
                    buf.readableBytes(), ByteBufUtil.hexDump(buf));
            ctx.fireChannelRead(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Netty Exception: ", cause);
            ctx.close();
        }
    }

}