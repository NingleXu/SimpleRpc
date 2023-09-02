package com.gdou.register.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.gdou.common.spi.ExtensionDirector;
import com.gdou.common.utils.RpcConfigReader;
import com.gdou.register.center.AbstractServiceDiscovery;
import com.gdou.register.loadbalance.LoadBalance;
import com.gdou.register.nacos.utils.NacosNamingServiceUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

import static com.gdou.common.constants.RpcConfigConstants.DEFAULT_LOADBALANCE;
import static com.gdou.common.constants.RpcConfigConstants.LOADBALANCE_KEY;

/**
 * @author ningle
 * @version : NacosDiscovery.java, v 0.1 2023/09/01 10:08 ningle
 **/
@Slf4j
public class NacosDiscoveryService extends AbstractServiceDiscovery {

    private final NamingService namingService;

    private String loadBalanceStrategy;

    // 从注册中心获取 可用实例的重试次数
    private static final int retryTimes = 3;
    // 重试时间 单位毫秒
    private static final int sleepMsBetweenRetries = 5000;


    public NacosDiscoveryService(InetSocketAddress address) {
        super(address);
        log.info("SimpleRpc 开始连接注册中心...");
        namingService = NacosNamingServiceUtil.createNamingService(address);

        if (namingService == null) {
            throw new RuntimeException(String.format("无法链接nacos注册中心, ip:%s,port:%s", address.getHostName(), address.getPort()));
        }
        log.info("SimpleRpc 注册中心nacos连接成功！");

        // 利用配置文件和SPI获取负载均衡策略
        // 配置对应的负载均衡策略  默认随机
        loadBalanceStrategy = RpcConfigReader.getConfigProperty(LOADBALANCE_KEY, DEFAULT_LOADBALANCE);
        loadBalance = ExtensionDirector
                .getExtensionLoader(LoadBalance.class)
                .getExtension(loadBalanceStrategy);
        log.info("SimpleRpc 负载均衡策略为:" + loadBalanceStrategy);
    }

    @Override
    public InetSocketAddress getService(String serviceName) {
        try {
            for (int i = 0; i < retryTimes + 1; i++) {
                List<Instance> instanceList = namingService.getAllInstances(serviceName);
                if (instanceList.isEmpty()) {
                    log.warn("SimpleRpc 注册中心nacos 无法找到[" + serviceName + "]的服务提供者，" +
                            (i == retryTimes ? "已达到最大重试次数！执行失败！" : "当前第" + (i + 1) + "次重试 , " +
                                    sleepMsBetweenRetries + "ms 后执行下次重试"));
                    Thread.sleep(sleepMsBetweenRetries);
                    continue;
                }
                Instance instance = getServiceByLoadBalance(instanceList);
                if (instance != null) {
                    return new InetSocketAddress(instance.getIp(), instance.getPort());
                }
                log.warn("SimpleRpc 无法找到合适的服务实例 当前负载均衡策略:" + loadBalanceStrategy +
                        (i == retryTimes ? " 已达到最大重试次数！执行失败！" : " 当前第" + (i + 1) + "次重试 , " +
                                sleepMsBetweenRetries + "ms 后执行下次重试"));
                Thread.sleep(sleepMsBetweenRetries);
            }
        } catch (NacosException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("SimpleRpc 无法找到可用的服务实例...");
    }


}
