package com.gdou.config.spring.service;

import com.gdou.common.annotations.RpcReference;

/**
 * @author ningle
 * @version : SpringServiceTest.java, v 0.1 2023/09/05 20:08 ningle
 **/
public class SpringServiceTest {

    @RpcReference
    private SpringService springService;

    public SpringService getSpringService() {
        return springService;
    }
}
