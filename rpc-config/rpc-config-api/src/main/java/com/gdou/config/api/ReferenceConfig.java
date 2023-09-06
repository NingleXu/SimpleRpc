package com.gdou.config.api;

import com.gdou.common.annotations.RpcReference;
import com.gdou.common.message.RpcRequestMessage;
import com.gdou.common.utils.SequenceIDGenerator;
import com.gdou.config.AbstractConfig;
import com.gdou.config.metadata.ServiceMetadata;
import com.gdou.config.api.netty.handler.RpcResponseMessageHandler;
import com.gdou.config.api.netty.server.RpcClientManager;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * @author ningle
 * @version : ReferenceConfig.java, v 0.1 2023/09/06 12:01 ningle
 **/
public class ReferenceConfig<T> extends AbstractConfig {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceConfig.class);

    // 服务元数据
    private ServiceMetadata metadata;

    /**
     * 代理类
     */
    private volatile T ref;

    private volatile boolean initialized;

    private RpcBootStrap bootStrap;

    public ReferenceConfig(RpcReference reference) {
        // 解析 reference 填充 serviceMetaData
        metadata = new ServiceMetadata(reference.version(), reference.group(), reference.interfaceClass());
    }

    public ReferenceConfig(Class<T> interfaceClazz, RpcReference reference) {
        // 解析 reference 填充 serviceMetaData
        metadata = new ServiceMetadata(reference.version(), reference.group(), interfaceClazz);
    }

    public ReferenceConfig(ServiceMetadata serviceMetadata) {
        // 解析 reference 填充 serviceMetaData
        metadata = serviceMetadata;
    }

    public T get() {
        if (ref == null) {
            init();
        }
        return ref;
    }


    public synchronized void init() {
        if (initialized) {
            return;
        }
        if (bootStrap == null) {
            bootStrap = RpcBootStrap.getInstance();
            bootStrap.initialize();
        }
        // 创建代理对象
        ref = createProxy();
        initialized = true;
    }

    private T createProxy() {
        Class<?> serviceType = metadata.getServiceType();
        ClassLoader classLoader = serviceType.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceType};

        String remoteServiceId = metadata.generateServiceId();

        Object proxyObj = Proxy.newProxyInstance(classLoader, interfaces, ((proxy, method, args) -> {
            int sequenceId = SequenceIDGenerator.nextId();
            // 将方法调用转换为消息
            RpcRequestMessage rpcRequestMessage = new RpcRequestMessage(
                    sequenceId,
                    serviceType.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args,
                    remoteServiceId
            );


            // 准备一个 空promise 对象 来接受结果
            DefaultPromise<Object> promise = new DefaultPromise<>(RpcClientManager.group.next());
            RpcResponseMessageHandler.PROMISES
                    .put(sequenceId, promise);

            if (bootStrap == null) {
                bootStrap = RpcBootStrap.getInstance();
                bootStrap.initialize();
            }

            Channel serverChannelByLoadBalance = RpcClientManager
                    .getServerChannelByLoadBalance(bootStrap, remoteServiceId);


            // 发送消息
            serverChannelByLoadBalance.writeAndFlush(rpcRequestMessage);
            // 等待结果
            promise.await(2, TimeUnit.SECONDS);
            if (promise.isSuccess()) {
                return promise.getNow();
            } else {
                logger.error("服务{}远程调用失败,等待服务响应超时", serviceType.getName());
                return null;
            }
        }));
        return (T) proxyObj;
    }

    public String getId() {
        return metadata.generateServiceId();
    }

}
