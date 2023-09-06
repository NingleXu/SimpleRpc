package com.gdou.config.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Rpc核心启动类
 *
 * @author ningle
 * @version : RpcBootStrap.java, v 0.1 2023/09/06 10:26 ningle
 **/
public class RpcBootStrap {

    private static final Logger logger = LoggerFactory.getLogger(RpcBootStrap.class);

    public volatile static RpcBootStrap instance;

    public final ConfigManager configManager;

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private RpcBootStrap() {
        configManager = new ConfigManager();
    }

    public synchronized static RpcBootStrap getInstance() {
        if (instance == null){
            instance = new RpcBootStrap();
        }
        return instance;
    }

    /**
     * 注册 service
     */
    public RpcBootStrap service(ServiceConfig<?> serviceConfig) {
        configManager.addService(serviceConfig);
        return this;
    }

    /**
     * 注册 refer
     */
    public RpcBootStrap reference(ReferenceConfig<?> referenceConfig) {
        configManager.addRefer(referenceConfig);
        return this;
    }


    public RpcBootStrap registry(InetSocketAddress address) {
        configManager.addRegistry(address);
        return this;
    }

    /**
     * 服务初始化
     */
    public void initialize() {
        // 开启注册中心
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        // 初始化 服务注册
        configManager.getRegistry().connect();
        // 初始化 服务发现
        configManager.getServiceDiscovery().connect();

        logger.info("SimpleRpc has been initialized");
    }

    public RpcBootStrap start() {
        if (started.compareAndSet(false, true)) {
            initialize();
            // service 发布
            exportServices();
            // reference service
            referServices();
        }
        return this;
    }

    private void referServices() {
        // 为 referService注册代理
        configManager.getRefers().forEach(ReferenceConfig::init);
    }

    private void exportServices() {
        configManager.getServices().forEach(ServiceConfig::export);
    }

}
