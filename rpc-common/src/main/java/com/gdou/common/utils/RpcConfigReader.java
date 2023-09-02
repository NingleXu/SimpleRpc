package com.gdou.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public abstract class RpcConfigReader {
    static Properties properties;

    static {
        try (InputStream in = RpcConfigReader.class.getResourceAsStream("/rpc-config.properties")) {
            properties = new Properties();
            if (Objects.isNull(in)) {
                log.debug("properties file is not exist");
            } else {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String getConfigProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static int getConfigProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

}