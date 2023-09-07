package com.gdou.config.spring.annotation;

import com.gdou.config.spring.RpcSpringInitializer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author ningle
 * @version : RpcConfigRegistrar.java, v 0.1 2023/09/07 14:15 ningle
 **/
public class RpcConfigRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // initialize dubbo beans
        RpcSpringInitializer.initialize(registry);
    }
}
