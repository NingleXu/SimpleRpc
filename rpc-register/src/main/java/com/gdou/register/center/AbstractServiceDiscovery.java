package com.gdou.register.center;


import com.gdou.register.loadbalance.LoadBalance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author ningle
 * @version : AbstractDiscovery.java, v 0.1 2023/09/01 10:06 ningle
 **/
public abstract class AbstractServiceDiscovery implements ServiceDiscovery {

    protected LoadBalance loadBalance;

    protected final InetSocketAddress address;

    public AbstractServiceDiscovery(InetSocketAddress address) {
        this.address = address;
    }

    protected <T> T getServiceByLoadBalance(List<T> instanceList) {
        return loadBalance.select(instanceList);
    }

}
