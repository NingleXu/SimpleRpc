package com.gdou.config.api;

import com.gdou.config.AbstractServiceConfig;
import com.gdou.config.metadata.ServiceMetadata;
import com.gdou.config.api.netty.server.RpcServerManager;
import io.netty.channel.Channel;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : ServiceConfig.java, v 0.1 2023/09/06 10:35 ningle
 **/
public class ServiceConfig<T> extends AbstractServiceConfig {

    // 服务元数据
    private ServiceMetadata metadata;

    private RpcBootStrap bootStrap;

    // 代理对象
    private T ref;

    public ServiceConfig(ServiceMetadata metadata, T ref) {
        this.metadata = metadata;
        this.ref = ref;
    }

    public ServiceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ServiceMetadata metadata) {
        this.metadata = metadata;
    }

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    public String getId() {
        return metadata.generateServiceId();
    }

    @Override
    public void export() {
        // 检查是否初始化完成
        if (bootStrap == null) {
            bootStrap = RpcBootStrap.getInstance();
            bootStrap.initialize();
        }

        // 获取服务端 channel
        Channel serverChannel = RpcServerManager.initIfNeed();

        // 注册到注册中心
        bootStrap.configManager
                .getRegistry().registerServer(getId(), (InetSocketAddress) serverChannel.localAddress());
    }
}
