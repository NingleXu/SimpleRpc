package com.gdou.register.nacos.utils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : NacosNamingServiceUtil.java, v 0.1 2023/09/01 18:32 ningle
 **/
@Slf4j
public class NacosNamingServiceUtil {

    // 最大尝试链接 nacos 次数
    private static final int retryTimes = 3;

    private static final int sleepMsBetweenRetries = 300;

    public static NamingService createNamingService(InetSocketAddress address) {
        NamingService namingService = null;

        try {
            for (int i = 0; i < retryTimes + 1; i++) {
                namingService = NamingFactory.createNamingService(address.getHostName() + ":" + address.getPort());
                String serverStatus = namingService.getServerStatus();

                boolean namingServiceAvailable = namingServiceAvailableTest(namingService);

                if (!namingServiceAvailable) {
                    log.warn("Failed to connect to nacos naming server. " +
                            "Server status: " + serverStatus + ". " +
                            "Naming Service Available: " + namingServiceAvailable + ". " +
                            (i < retryTimes ? "SimpleRpc will try to retry in " + sleepMsBetweenRetries + ". " : "Exceed retry max times.") +
                            "Try times: " + (i + 1));
                } else {
                    break;
                }
                namingService = null;
                Thread.sleep(sleepMsBetweenRetries);
            }
        } catch (InterruptedException | NacosException e) {
            throw new RuntimeException(e);
        }
        return namingService;
    }

    /**
     * 测试建立的链接有效性
     *
     * @param namingService 创建的 namingService
     * @return 是否链接成功
     */
    private static boolean namingServiceAvailableTest(NamingService namingService) {
        try {
            namingService.getAllInstances("RPC-REGISTRY-EFFECTIVENESS-TEST", false);
        } catch (NacosException ignore) {
            return false;
        }
        return true;
    }

}
