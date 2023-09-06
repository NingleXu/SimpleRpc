package com.gdou.test;

import com.gdou.config.api.RpcBootStrap;
import com.gdou.config.api.ServiceConfig;
import com.gdou.config.api.ServiceConfigBuilder;
import com.gdou.test.service.HelloService;
import com.gdou.test.service.impl.HelloServiceImpl;

import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : TestService.java, v 0.1 2023/08/31 17:11 ningle
 **/
public class TestService {
    public static void main(String[] args) {
        ServiceConfig<HelloService> serviceConfig = new ServiceConfigBuilder<HelloService>()
                .group("")
                .version("0.0.1")
                .interfaceClass(HelloService.class)
                .ref(new HelloServiceImpl())
                .build();
        RpcBootStrap
                .getInstance()
                .registry(new InetSocketAddress("localhost", 8848))
                .service(serviceConfig)
                .start();
    }
}
