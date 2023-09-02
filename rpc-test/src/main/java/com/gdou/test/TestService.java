package com.gdou.test;

import com.gdou.core.RpcServerBootStrap;

import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : TestService.java, v 0.1 2023/08/31 17:11 ningle
 **/
public class TestService {
    public static void main(String[] args) {
        // 服务器端运行
        RpcServerBootStrap.getInstance()
                // 注册中心 目前仅支持 nacos
                .registry(new InetSocketAddress("localhost", 8848))
                .start()
                .await();
    }
}
