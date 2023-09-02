package com.gdou.test.service.impl;

import com.gdou.common.annotations.RpcService;
import com.gdou.test.service.HelloService;

/**
 * @author ningle
 * @version : HelloServiceImpl.java, v 0.1 2023/08/31 16:44 ningle
 **/
@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "你好吖，我是服务3!" + name;
    }
}
