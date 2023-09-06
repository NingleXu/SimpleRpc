package com.gdou.register.center;


import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : AbstractRegistry.java, v 0.1 2023/09/01 09:43 ningle
 * <p>
 * 抽象的服务注册中心
 **/
public abstract class AbstractRegistry implements Registry {

    protected InetSocketAddress address;

    public AbstractRegistry(InetSocketAddress address) {
        this.address = address;
    }

}
