package com.gdou.config.spring.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 *
 * 负责扫描 RpcService 标注的类，将其加载为 BeanDefinition
 *
 * @author ningle
 * @version : RpcServiceBeanDefinitionRegister.java, v 0.1 2023/09/05 15:21 ningle
 **/
public class RpcServiceBeanDefinitionRegister implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        /**
         * 扫描所有的注册
         */

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
