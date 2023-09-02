package com.gdou.register.center;

import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : Discovery.java, v 0.1 2023/09/01 10:12 ningle
 **/
public interface ServiceDiscovery {
    InetSocketAddress getService(String serviceName);

}
