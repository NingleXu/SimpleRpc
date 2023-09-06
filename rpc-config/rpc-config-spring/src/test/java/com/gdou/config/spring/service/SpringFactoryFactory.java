package com.gdou.config.spring.service;

import com.gdou.config.spring.annotation.ReferenceAnnotationBeanPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @author ningle
 * @version : SpringFactoryFactory.java, v 0.1 2023/09/05 20:09 ningle
 **/
public class SpringFactoryFactory {
    @Test
    public void testRpcReferenceAnnotation() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        beanFactory.registerBeanDefinition("springServiceTest",
                BeanDefinitionBuilder
                        .genericBeanDefinition(SpringServiceTest.class)
                        .getBeanDefinition());


        beanFactory.registerBeanDefinition("ReferenceAnnotationBeanPostProcessor",
                BeanDefinitionBuilder
                        .genericBeanDefinition(ReferenceAnnotationBeanPostProcessor.class)
                        .getBeanDefinition());

        ReferenceAnnotationBeanPostProcessor beanPostProcessor
                = beanFactory.getBean(ReferenceAnnotationBeanPostProcessor.class);

        beanFactory.addBeanPostProcessor(beanPostProcessor);

        SpringServiceTest bean = beanFactory.getBean(SpringServiceTest.class);

        SpringService springService = bean.getSpringService();
        System.out.println(11);
    }
}
