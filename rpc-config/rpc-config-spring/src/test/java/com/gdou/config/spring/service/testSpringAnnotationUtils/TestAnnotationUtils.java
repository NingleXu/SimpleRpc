package com.gdou.config.spring.service.testSpringAnnotationUtils;

import com.gdou.common.annotations.RpcReference;
import com.gdou.config.spring.service.SpringServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.reflect.Field;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;

/**
 * @author ningle
 * @version : TestAnnotationUtils.java, v 0.1 2023/09/06 17:04 ningle
 **/
public class TestAnnotationUtils {
    @Test
    public void test1() {
        Field springService;
        try {
            springService = SpringServiceTest.class.getDeclaredField("springService");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        AnnotationAttributes annotationAttributes = getAnnotationAttributes(springService, springService.getAnnotation(RpcReference.class));

        System.out.println(annotationAttributes);

    }
}
