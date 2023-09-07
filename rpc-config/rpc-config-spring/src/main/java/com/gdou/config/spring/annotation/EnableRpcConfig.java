package com.gdou.config.spring.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author ningle
 * @version : EnableRpcConfig.java, v 0.1 2023/09/07 14:13 ningle
 **/
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RpcConfigRegistrar.class)
public @interface EnableRpcConfig {
}
