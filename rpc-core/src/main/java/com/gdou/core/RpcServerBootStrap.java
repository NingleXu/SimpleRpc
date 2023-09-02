package com.gdou.core;

import com.gdou.common.spi.ExtensionDirector;
import com.gdou.common.utils.RpcConfigReader;
import com.gdou.netty.config.ServiceAnnotationsScanner;
import com.gdou.netty.server.RpcServerManager;
import com.gdou.register.center.Registry;
import com.gdou.register.factory.RegistryFactory;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Set;

import static com.gdou.common.constants.RpcConfigConstants.DEFAULT_REGISTRY;
import static com.gdou.common.constants.RpcConfigConstants.REGISTRY_KEY;

/**
 * @author ningle
 * @version : RpcServerBootStrap.java, v 0.1 2023/08/31 15:49 ningle
 **/
@Slf4j
public class RpcServerBootStrap {

    private static volatile RpcServerBootStrap instance;

    // 注册中心
    private Registry registry;

    private Channel nettyChannel;

    private RpcServerBootStrap() {
        // 触发服务端扫描 所有被注解修饰的类
        ServiceAnnotationsScanner.searchAnnotatedClass();
    }

    public static RpcServerBootStrap getInstance() {
        if (instance == null) {
            synchronized (RpcServerBootStrap.class) {
                if (instance == null) {
                    instance = new RpcServerBootStrap();
                }
            }
        }
        return instance;
    }

    public RpcServerBootStrap registry(InetSocketAddress address) {

        // 配置文件读取注册中心类型
        String registryConfigProperty = RpcConfigReader
                .getConfigProperty(REGISTRY_KEY, DEFAULT_REGISTRY);
        log.info("SimpleRpc 当前注册中心类型为" + registryConfigProperty);
        // 利用SPI 获取注册中心工厂
        RegistryFactory registryFactory = ExtensionDirector
                .getExtensionLoader(RegistryFactory.class)
                .getExtension(registryConfigProperty);
        // 工厂获取对应的实现
        registry = registryFactory.getRegistry(address);

        return this;
    }

    public RpcServerBootStrap start() {
        // 初始化 NioServerSocketChannel
        nettyChannel = RpcServerManager.init();
        // 获取服务器地址
        InetSocketAddress serverSocketAddress = (InetSocketAddress) nettyChannel.localAddress();

        log.info("服务端部署成功！ip:{} 端口号:{}", serverSocketAddress.getHostName(), serverSocketAddress.getPort());

        // 将服务注册到注册中心
        Set<String> serverNames = ServiceAnnotationsScanner.annotatedClassMap.keySet();

        // 注册到注册中心
        registry.registerServer(serverNames, serverSocketAddress);

        return this;
    }

    public void await() {
        try {
            nettyChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
