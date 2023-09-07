package com.gdou.config.spring.service.service;

import com.gdou.config.api.ConfigManager;
import com.gdou.config.api.RpcBootStrap;
import com.gdou.config.spring.service.SpringServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author ningle
 * @version : SpringFactoryFactory.java, v 0.1 2023/09/05 20:09 ningle
 **/
public class SpringFactoryFactory {
    @Test
    public void testRpcReferenceAnnotation() {
        GenericApplicationContext applicationContext = new GenericApplicationContext();

        applicationContext.registerBean(SpringServiceTest.class);
        applicationContext.registerBean(ConfigurationClassPostProcessor.class);
        applicationContext.refresh();


        ConfigManager configManager = RpcBootStrap
                .getInstance()
                .configManager;
        for (String name : applicationContext.getBeanDefinitionNames()) {
            System.out.println(name);
        }


    }
}
