package com.gdou.test;

import com.gdou.core.RpcClientBootStrap;
import com.gdou.test.service.HelloService;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author ningle
 * @version : TestClient.java, v 0.1 2023/08/31 17:11 ningle
 **/
public class TestClient {
    public static void main(String[] args) {
        RpcClientBootStrap rpcClientConfig = RpcClientBootStrap.getInstance()
                .registry(new InetSocketAddress("localhost", 8848));
        HelloService helloService = rpcClientConfig.getProxy(HelloService.class);
        while (true) {
            String s = helloService.sayHello("ningle");
            System.out.println(s);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
