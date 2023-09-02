package com.gdou.register.nacos;

import com.gdou.register.center.ServiceDiscovery;
import com.gdou.register.factory.AbstractServiceDiscoveryFactory;

import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : NacosServiceDiscoveryFactory.java, v 0.1 2023/09/01 11:46 ningle
 **/
public class NacosServiceDiscoveryFactory extends AbstractServiceDiscoveryFactory {
    @Override
    public ServiceDiscovery createDiscovery(InetSocketAddress address) {
        return new NacosDiscoveryService(address);
    }
}
