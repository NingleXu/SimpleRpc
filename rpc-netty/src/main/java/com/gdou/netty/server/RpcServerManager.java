package com.gdou.netty.server;

import com.gdou.netty.handler.RpcRequestMessageHandler;
import com.gdou.netty.protocol.MessageCodec;
import com.gdou.netty.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcServerManager {
    public static Channel init() {
        MessageCodec codecHandler = new MessageCodec();
        RpcRequestMessageHandler RPC_REQUEST_HANDLER = new RpcRequestMessageHandler();

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup works = new NioEventLoopGroup();

        Channel channel = null;
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .channel(NioServerSocketChannel.class)
                    .group(boss, works)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel sc) throws Exception {
                            sc.pipeline()
                                    .addLast(new ProtocolFrameDecoder())
                                    .addLast(codecHandler)
                                    .addLast(RPC_REQUEST_HANDLER);
                        }
                    });

            channel = serverBootstrap
                    .bind(0).sync()
                    .channel();
            channel.closeFuture().addListener((ChannelFutureListener) channelFuture -> {
                boss.shutdownGracefully();
                works.shutdownGracefully();
                log.debug("server is close");
            });
        } catch (InterruptedException e) {
            log.debug("server error , {}", e.getMessage());
        }
        return channel;
    }
}
