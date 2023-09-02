package com.gdou.common.spi;


import com.gdou.common.utils.Holder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.gdou.common.constants.RpcConfigConstants.SPI_PATH;

/**
 * @author ningle
 * @version : ExtensionLoader.java, v 0.1 2023/09/01 11:22 ningle
 * <p>
 * 模拟dubbo SPI 获取对应的实现类
 **/
public class ExtensionLoader<T> {

    private final Class<?> type;

    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();


    ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        final Holder<Object> holder = getOrCreateHolder(name);
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("未能找到[" + name + "]拓展类");
        }

        try {
            return (T) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Holder<Object> getOrCreateHolder(String name) {
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        return holder;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classMap = cachedClasses.get();
        // 双端检锁
        if (classMap == null) {
            synchronized (cachedClasses) {
                classMap = cachedClasses.get();
                if (classMap == null) {
                    classMap = loadExtensionClasses();
                }
            }
        }
        return classMap;
    }

    private Map<String, Class<?>> loadExtensionClasses() {

        Map<String, Class<?>> classMap = new HashMap<>();
        String fileName = SPI_PATH + type.getName();

        // 构建URL
        URL resourceURL = ClassLoader.getSystemResource(fileName);

        if (Objects.isNull(resourceURL)) {
            throw new RuntimeException(String.format("文件路径【%s】不存在", fileName));
        }

        // 读取文件内容
        List<String> resourceContent = getResourceContent(resourceURL);

        // 加载文件内容中的类
        loadResource(classMap, resourceContent, resourceURL);
        return classMap;
    }

    private void loadResource(Map<String, Class<?>> classMap, List<String> resourceContent, URL resourceURL) {

        String clazz;
        for (String line : resourceContent) {

            try {
                String name = null;
                int i = line.indexOf('=');
                if (i > 0) {
                    name = line.substring(0, i).trim();
                    clazz = line.substring(i + 1).trim();
                } else {
                    clazz = line;
                }

                if (!clazz.isEmpty()) {
                    Class<?> implClazz = Class.forName(clazz, true, Thread.currentThread().getContextClassLoader());
                    classMap.put(name, implClazz);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> getResourceContent(URL resourceURL) {
        ArrayList<String> contentList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    contentList.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return contentList;
    }

}
