package com.gdou.config.spring.service.refence;

import com.gdou.config.api.ConfigManager;
import com.gdou.config.api.RpcBootStrap;
import com.gdou.config.spring.service.SpringServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author ningle
 * @version : ReferenceTest.java, v 0.1 2023/09/07 14:09 ningle
 **/
public class ReferenceTest {

    @Test
    public void test1() {
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
