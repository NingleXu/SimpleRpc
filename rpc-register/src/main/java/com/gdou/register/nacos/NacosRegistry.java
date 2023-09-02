package com.gdou.register.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.gdou.register.center.AbstractRegistry;
import com.gdou.register.nacos.utils.NacosNamingServiceUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * @author ningle
 * @version : NacosServerRegistry.java, v 0.1 2023/09/01 09:50 ningle
 **/
@Slf4j
public class NacosRegistry extends AbstractRegistry {
    private NamingService namingService;

    public NacosRegistry(InetSocketAddress address) {
        super(address);
        log.info("SimpleRpc 开始连接注册中心...");
        namingService = NacosNamingServiceUtil.createNamingService(address);

        if (namingService == null) {
            throw new RuntimeException(String.format("无法链接nacos注册中心, ip:%s,port:%s", address.getHostName(), address.getPort()));
        }

        log.info("SimpleRpc 注册中心nacos连接成功！");
    }

    @Override
    public void registerServer(String serverName, InetSocketAddress address) {
        try {
            namingService.registerInstance(serverName, address.getHostName(), address.getPort());
        } catch (NacosException e) {
            e.printStackTrace();
            throw new RuntimeException("服务[" + serverName + "]注册到nacos失败");
        }
    }

    @Override
    public void registerServer(Collection<String> serverCollection, InetSocketAddress address) {
        serverCollection.forEach(serverName -> registerServer(serverName, address));
    }
}
