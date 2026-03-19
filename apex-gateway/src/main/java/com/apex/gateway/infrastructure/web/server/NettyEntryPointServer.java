package com.apex.gateway.infrastructure.web.server;

import com.apex.gateway.infrastructure.web.server.decode.B3EntryPointDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
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

    @Value("${apex-engine.server.netty.entrypoint.port}")
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

}