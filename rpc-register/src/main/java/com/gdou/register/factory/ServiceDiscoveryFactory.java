package com.gdou.register.factory;

import com.gdou.register.center.ServiceDiscovery;

import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : ServiceDiscoveryFactory.java, v 0.1 2023/09/01 11:42 ningle
 * <p>
 * 服务发现工厂
 **/
public interface ServiceDiscoveryFactory {
    ServiceDiscovery createDiscovery(InetSocketAddress address);

}
