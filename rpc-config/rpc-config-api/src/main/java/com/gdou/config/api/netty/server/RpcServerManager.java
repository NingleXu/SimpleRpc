package com.gdou.config.api.netty.server;

import com.gdou.config.api.netty.handler.RpcRequestMessageHandler;
import com.gdou.config.api.netty.protocol.MessageCodec;
import com.gdou.config.api.netty.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcServerManager {

    private static volatile Channel serverChannel;

    public synchronized static Channel initIfNeed() {

        if (serverChannel != null) {
            return serverChannel;
        }

        MessageCodec codecHandler = new MessageCodec();
        RpcRequestMessageHandler RPC_REQUEST_HANDLER = new RpcRequestMessageHandler();

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup works = new NioEventLoopGroup();

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

            serverChannel = serverBootstrap
                    .bind(0).sync()
                    .channel();
            serverChannel.closeFuture().addListener((ChannelFutureListener) channelFuture -> {
                boss.shutdownGracefully();
                works.shutdownGracefully();
                log.debug("server is close");
            });
        } catch (InterruptedException e) {
            log.debug("server error , {}", e.getMessage());
        }
        return serverChannel;
    }
}
