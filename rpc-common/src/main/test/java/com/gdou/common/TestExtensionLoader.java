package com.gdou.common;

import com.gdou.common.service.TestService;
import com.gdou.common.service.TestServiceImpl1;
import com.gdou.common.spi.ExtensionDirector;
import org.junit.jupiter.api.Test;


/**
 * @author ningle
 * @version : TestExtensionLoader.java, v 0.1 2023/09/01 12:17 ningle
 **/
public class TestExtensionLoader {

    @Test
    public void testgetExtension() {
        TestService test1 = ExtensionDirector
                .getExtensionLoader(TestService.class)
                .getExtension("test1");

        assert test1 instanceof TestServiceImpl1;
    }
}