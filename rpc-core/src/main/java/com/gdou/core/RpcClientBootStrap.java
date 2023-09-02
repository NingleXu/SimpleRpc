package com.gdou.core;

import com.gdou.common.message.RpcRequestMessage;
import com.gdou.common.spi.ExtensionDirector;
import com.gdou.common.utils.RpcConfigReader;
import com.gdou.common.utils.SequenceIDGenerator;
import com.gdou.netty.handler.RpcResponseMessageHandler;
import com.gdou.register.center.ServiceDiscovery;
import com.gdou.register.factory.ServiceDiscoveryFactory;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.gdou.common.constants.RpcConfigConstants.DEFAULT_REGISTRY;
import static com.gdou.common.constants.RpcConfigConstants.REGISTRY_KEY;
import static com.gdou.netty.server.RpcClientManager.getChannel;
import static com.gdou.netty.server.RpcClientManager.group;

/**
 * @author ningle
 * @version : RpcClientBootStrap.java, v 0.1 2023/09/01 14:13 ningle
 **/
@Slf4j
public class RpcClientBootStrap {

    private static volatile RpcClientBootStrap instance;

    // 服务发现
    private static ServiceDiscovery serviceDiscovery;

    public static RpcClientBootStrap getInstance() {
        if (instance == null) {
            synchronized (RpcClientBootStrap.class) {
                if (instance == null) {
                    instance = new RpcClientBootStrap();
                }
            }
        }
        return instance;
    }

    private RpcClientBootStrap() {
    }


    public RpcClientBootStrap registry(InetSocketAddress address) {
        // 配置文件读取注册中心类型
        String registryConfigProperty = RpcConfigReader
                .getConfigProperty(REGISTRY_KEY, DEFAULT_REGISTRY);

        ServiceDiscoveryFactory serviceDiscoveryFactory = ExtensionDirector
                .getExtensionLoader(ServiceDiscoveryFactory.class)
                .getExtension(registryConfigProperty);
        serviceDiscovery = serviceDiscoveryFactory.createDiscovery(address);

        return this;
    }

    /**
     * 获取代理对象
     *
     * @param serviceClass
     * @param <T>
     * @return
     */

    public <T> T getProxy(Class<T> serviceClass) {
        ClassLoader classLoader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};

        Object proxyObj = Proxy.newProxyInstance(classLoader, interfaces, ((proxy, method, args) -> {
            int sequenceId = SequenceIDGenerator.nextId();
            // 将方法调用转换为消息
            RpcRequestMessage rpcRequestMessage = new RpcRequestMessage(
                    sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
            );


            // 准备一个 空promise 对象 来接受结果
            DefaultPromise<Object> promise = new DefaultPromise<>(group.next());
            RpcResponseMessageHandler.PROMISES
                    .put(sequenceId, promise);

            Channel serverChannel = getServerChannel(serviceClass.getName());

            // 发送消息
            serverChannel.writeAndFlush(rpcRequestMessage);
            // 等待结果
            promise.await(2, TimeUnit.SECONDS);
            if (promise.isSuccess()) {
                return promise.getNow();
            } else {
                log.error("服务{}远程调用失败,等待服务响应超时", serviceClass.getName());
                return null;
            }
        }));
        return (T) proxyObj;
    }

    /**
     * 获取服务提供端的连接
     *
     * @param serverName 服务名称
     * @return 可供调用的连接
     */
    private Channel getServerChannel(String serverName) {
        Channel channel = null;

        while (channel == null) {
            // 获取负载均衡所找到的服务端地址
            InetSocketAddress inetSocketAddress = serviceDiscovery.getService(serverName);

            // 客户端与其建立链接获取channel
            // channel 为 null 说明这个连接不可用，重新选择
            channel = getChannel(inetSocketAddress);
        }
        return channel;
    }

}
