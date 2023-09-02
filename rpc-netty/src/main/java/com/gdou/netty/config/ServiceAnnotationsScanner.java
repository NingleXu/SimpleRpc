package com.gdou.netty.config;


import com.gdou.common.annotations.RpcService;
import com.gdou.common.utils.RpcConfigReader;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.gdou.common.constants.RpcConfigConstants.DEFAULT_SCAN_PACKAGES;
import static com.gdou.common.constants.RpcConfigConstants.SCAN_PACKAGES_KEY;

/**
 * @author ningle
 * @version : ServiceAnnotationsScanner.java, v 0.1 2023/08/30 19:51 ningle
 **/
@Slf4j
public class ServiceAnnotationsScanner {
    public static final Map<String, Class<?>> annotatedClassMap = new HashMap<>();

    public static final Map<Class<?>, Object> CLASS_OBJECT_MAP = new HashMap<>();

    public static void searchAnnotatedClass() {
        log.info("SimpleRpc 开始执行扫描服务");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String scanPackages = RpcConfigReader.getConfigProperty(SCAN_PACKAGES_KEY, DEFAULT_SCAN_PACKAGES);

        URL resource = classLoader.getResource(scanPackages.replace('.', '/'));

        findByFilePathClass(new File(resource.getFile()), scanPackages);

        log.info("SimpleRpc 扫描到" + annotatedClassMap.size() + "个服务");
    }

    @SuppressWarnings("unchecked")
    public static <T> T getOrCreateClassObject(Class<T> clazz) {
        return (T) CLASS_OBJECT_MAP
                .computeIfAbsent(clazz, key -> {
                    try {
                        return clazz.getConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void findByFilePathClass(File rootFile, String currentPath) {
        if (!rootFile.exists()) {
            return;
        }

        if (rootFile.isDirectory()) {
            File[] files = rootFile.listFiles();
            for (File listFile : files) {
                findByFilePathClass(listFile, nextPath(currentPath, listFile.getName()));
            }
        } else {
            try {
                if (!currentPath.endsWith(".class")) {
                    return;
                }
                Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(currentPath.substring(0, currentPath.length() - 6));
                if (clazz.isInterface()) {
                    return;
                }
                if (!clazz.isAnnotationPresent(RpcService.class)) {
                    return;
                }

                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> interfaceClazz : interfaces) {
                    String interfaceClazzName = interfaceClazz.getName();
                    if (annotatedClassMap.containsKey(interfaceClazzName)) {
                        throw new RuntimeException("接口【" + interfaceClazz + "】存在多个实现类！");
                    }
                    annotatedClassMap.put(interfaceClazzName, clazz);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }


    }

    private static String nextPath(String currentPath, String fileName) {
        if (currentPath.isEmpty()) {
            return fileName;
        }
        return currentPath + "." + fileName;
    }

}



