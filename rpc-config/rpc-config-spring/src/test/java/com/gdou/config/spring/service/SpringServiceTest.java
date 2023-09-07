package com.gdou.config.spring.service;

import com.gdou.common.annotations.RpcReference;
import com.gdou.common.annotations.RpcService;
import com.gdou.config.spring.annotation.RpcComponentScan;

/**
 * @author ningle
 * @version : SpringServiceTest.java, v 0.1 2023/09/05 20:08 ningle
 **/
@RpcService
@RpcComponentScan
public class SpringServiceTest implements SpringService {

    @RpcReference(version = "0.0.1")
    private SpringService springService;

    public SpringService getSpringService() {
        return springService;
    }

    @Override
    public String spring() {
        return null;
    }
}
