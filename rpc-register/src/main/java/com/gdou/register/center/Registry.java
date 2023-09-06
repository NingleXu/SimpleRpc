package com.gdou.register.center;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * @author ningle
 * @version : Registry.java, v 0.1 2023/09/01 10:10 ningle
 **/
public interface Registry {

    void connect();

    void registerServer(String serverName, InetSocketAddress address);

    void registerServer(Collection<String> serverName, InetSocketAddress address);
}
