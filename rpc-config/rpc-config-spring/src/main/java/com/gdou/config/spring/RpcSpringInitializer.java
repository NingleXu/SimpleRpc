package com.gdou.config.spring;

import com.gdou.config.api.RpcBootStrap;
import com.gdou.config.spring.annotation.ReferenceAnnotationBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.net.InetSocketAddress;

/**
 * @author ningle
 * @version : RpcSpringInitializer.java, v 0.1 2023/09/07 14:16 ningle
 **/
public class RpcSpringInitializer {
    public final static Logger logger = LoggerFactory.getLogger(RpcSpringInitializer.class);

    /**
     * 注册所有的配置
     */
    public static void initialize(BeanDefinitionRegistry registry) {

        // reference
        registerInfrastructureBean(registry, ReferenceAnnotationBeanPostProcessor.BEAN_NAME,
                ReferenceAnnotationBeanPostProcessor.class);

        // 注册中心 和 服务发现
        RpcBootStrap
                .getInstance()
                .configManager
                .addRegistry(new InetSocketAddress("localhost", 8848));

        //  Register RpcBootstrapApplicationListener as an infrastructure Bean
        registerInfrastructureBean(registry, RpcBootstrapApplicationListener.BEAN_NAME,
                RpcBootstrapApplicationListener.class);

    }

    public static boolean registerInfrastructureBean(BeanDefinitionRegistry beanDefinitionRegistry,
                                                     String beanName,
                                                     Class<?> beanType) {

        boolean registered = false;

        if (!beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            RootBeanDefinition beanDefinition = new RootBeanDefinition(beanType);
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
            registered = true;

            if (logger.isInfoEnabled()) {
                logger.info("The Infrastructure bean definition [" + beanDefinition
                        + "with name [" + beanName + "] has been registered.");
            }
        }

        return registered;
    }

}
