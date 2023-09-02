package com.gdou.common.spi;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ningle
 * @version : ExtensionDirector.java, v 0.1 2023/09/01 11:23 ningle
 * <p>
 * SPI Director
 **/
public class ExtensionDirector {

    /**
     * 加载器的map集合
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> extensionLoadersMap = new ConcurrentHashMap<>(64);

    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) extensionLoadersMap.get(type);
        if (Objects.isNull(extensionLoader)) {
            extensionLoader = createExtensionLoader(type);
        }
        return extensionLoader;
    }

    @SuppressWarnings("unchecked")
    private static <T> ExtensionLoader<T> createExtensionLoader(Class<T> type) {
        ExtensionLoader<T> loader;
        extensionLoadersMap.putIfAbsent(type, new ExtensionLoader<T>(type));
        loader = (ExtensionLoader<T>) extensionLoadersMap.get(type);
        return loader;
    }

}
