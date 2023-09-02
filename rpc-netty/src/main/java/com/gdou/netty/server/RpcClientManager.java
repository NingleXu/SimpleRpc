package com.gdou.netty.server;

import com.gdou.netty.handler.RpcResponseMessageHandler;
import com.gdou.netty.protocol.MessageCodec;
import com.gdou.netty.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcClientManager {
    private volatile static Bootstrap bootstrap = null;
    private static final Object LOCK = new Object();

    public static NioEventLoopGroup group = new NioEventLoopGroup();
    public final static Map<String, Channel> channels = new ConcurrentHashMap<>();

    public static Channel getChannel(InetSocketAddress inetSocketAddress) {

        // 由于连接可以复用 ， 查询本地是否存在连接过的channel
        String key = inetSocketAddress.toString();
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            }
            channels.remove(key);
        }

        // 尝试通过建立客户端与服务器端链接
        Channel channel = null;
        try {
            channel = getClient().connect(inetSocketAddress).sync().channel();
            channel.closeFuture().addListener(future -> log.debug("断开连接"));
        } catch (Exception e) {
            // 链接建立失败， 有可能是服务器关闭了导致的
            log.error("连接客户端出错, 原因: " + e.getMessage());
            // 返回 null ,交给其重新选择channel
            return null;
        }
        // 建立成功 将其缓存起来
        channels.put(key, channel);
        return channel;
    }

    /**
     * 获取单例客户端
     *
     * @return 返回客户端对象
     */
    private static Bootstrap getClient() {
        if (bootstrap == null) {
            synchronized (LOCK) {
                if (bootstrap == null) {
                    bootstrap = initClient();
                }
            }
        }

        return bootstrap;
    }

    /**
     * 初始化客户端
     */
    private static Bootstrap initClient() {
        MessageCodec codecHandler = new MessageCodec();
        ChannelInboundHandlerAdapter RPC_RESPONSE_HANDLER = new RpcResponseMessageHandler();

        return new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(group)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel sc) {
                        sc.pipeline()
                                .addLast(new ProtocolFrameDecoder())
                                .addLast(codecHandler)
                                .addLast(RPC_RESPONSE_HANDLER);
                    }
                });
    }

}
