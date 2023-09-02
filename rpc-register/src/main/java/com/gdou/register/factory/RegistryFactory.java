package com.gdou.register.factory;

import com.gdou.register.center.Registry;

import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : RegistryFactory.java, v 0.1 2023/09/01 11:51 ningle
 **/
public interface RegistryFactory {

    Registry getRegistry(InetSocketAddress address);

}
