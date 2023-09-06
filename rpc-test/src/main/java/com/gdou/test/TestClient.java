package com.gdou.test;

import com.gdou.config.api.ReferenceConfig;
import com.gdou.config.api.ReferenceConfigBuilder;
import com.gdou.config.api.RpcBootStrap;
import com.gdou.test.service.HelloService;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : TestClient.java, v 0.1 2023/08/31 17:11 ningle
 **/
public class TestClient {
    public static void main(String[] args) {
        ReferenceConfig<HelloService> referenceConfig = new ReferenceConfigBuilder<HelloService>()
                .interfaceClass(HelloService.class)
                .group("")
                .version("0.0.1")
                .build();

        RpcBootStrap
                .getInstance()
                .registry(new InetSocketAddress("localhost", 8848))
                .reference(referenceConfig)
                .start();

        HelloService helloService = referenceConfig.get();
        System.out.println(helloService.sayHello("ningle"));
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
