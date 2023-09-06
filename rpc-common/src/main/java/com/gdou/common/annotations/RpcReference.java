package com.gdou.common.annotations;

import java.lang.annotation.*;

/**
 * @author ningle
 * @version : RpcReference.java, v 0.1 2023/09/05 16:43 ningle
 **/
@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {

    /**
     * Interface class, default value is void.class
     */
    Class<?> interfaceClass() default void.class;

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

}
