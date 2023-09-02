package com.gdou.register.nacos;

import com.gdou.register.center.Registry;
import com.gdou.register.factory.AbstractRegistryFactory;

import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : NacosRegistryFactory.java, v 0.1 2023/09/01 11:54 ningle
 **/
public class NacosRegistryFactory extends AbstractRegistryFactory {

    @Override
    public Registry getRegistry(InetSocketAddress address) {
        return new NacosRegistry(address);
    }
}
