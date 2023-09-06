package com.gdou.config.api;

import com.gdou.common.spi.ExtensionDirector;
import com.gdou.common.utils.RpcConfigReader;
import com.gdou.register.center.Registry;
import com.gdou.register.center.ServiceDiscovery;
import com.gdou.register.factory.RegistryFactory;
import com.gdou.register.factory.ServiceDiscoveryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.gdou.common.constants.RpcConfigConstants.DEFAULT_REGISTRY;
import static com.gdou.common.constants.RpcConfigConstants.REGISTRY_KEY;

/**
 * @author ningle
 * @version : ConfigManager.java, v 0.1 2023/09/06 10:58 ningle
 **/
public class ConfigManager {

    Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    /**
     * 所有服务缓存
     */
    private final Map<String, ServiceConfig<?>> servicesMap = new HashMap<>();

    /**
     * 所有reference 缓存
     */
    private final Map<String, ReferenceConfig<?>> refersMap = new HashMap<>();

    /**
     * 服务发现配置
     */
    private ServiceDiscovery serviceDiscovery;

    /**
     * 服务注册中心
     */
    private Registry registry;

    public ConfigManager() {
    }

    public void addService(ServiceConfig<?> serviceConfig) {
        servicesMap.put(serviceConfig.getId(), serviceConfig);
    }

    public void addRefer(ReferenceConfig<?> referenceConfig) {
        refersMap.put(referenceConfig.getId(), referenceConfig);
    }

    public void addRegistry(InetSocketAddress address) {
        // 配置文件读取注册中心类型
        String registryConfigProperty = RpcConfigReader
                .getConfigProperty(REGISTRY_KEY, DEFAULT_REGISTRY);
        logger.info("SimpleRpc 当前注册中心类型为" + registryConfigProperty);
        // 利用SPI 获取注册中心工厂
        RegistryFactory registryFactory = ExtensionDirector
                .getExtensionLoader(RegistryFactory.class)
                .getExtension(registryConfigProperty);
        // 工厂获取对应的实现
        registry = registryFactory.getRegistry(address);

        // 服务发现
        ServiceDiscoveryFactory serviceDiscoveryFactory = ExtensionDirector
                .getExtensionLoader(ServiceDiscoveryFactory.class)
                .getExtension(registryConfigProperty);
        serviceDiscovery = serviceDiscoveryFactory.createDiscovery(address);
    }

    public Collection<ServiceConfig<?>> getServices() {
        return servicesMap.values();
    }

    public ServiceConfig<?> getService(String serviceId) {
        return servicesMap.get(serviceId);
    }

    public Collection<ReferenceConfig<?>> getRefers(){
        return refersMap.values();
    }

    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    public Registry getRegistry() {
        return registry;
    }
}
