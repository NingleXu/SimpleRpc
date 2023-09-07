package com.gdou.config.api;

import com.gdou.common.config.metadata.ServiceMetadata;

/**
 * @author ningle
 * @version : ReferenceConfigBuilder.java, v 0.1 2023/09/06 16:15 ningle
 **/
public class ReferenceConfigBuilder<T> {

    private String version;

    private String group;

    private Class<?> serviceType;

    public static ReferenceConfigBuilder newBuilder() {
        return new ReferenceConfigBuilder();
    }

    public ReferenceConfigBuilder<T> version(String version) {
        this.version = version;
        return this;
    }

    public ReferenceConfigBuilder<T> group(String group) {
        this.group = group;
        return this;
    }

    public ReferenceConfigBuilder<T> interfaceClass(Class<?> interfaceClazz) {
        this.serviceType = interfaceClazz;
        return this;
    }


    public ReferenceConfig<T> build() {
        ServiceMetadata serviceMetadata = new ServiceMetadata(this.version, this.group, this.serviceType);
        return new ReferenceConfig<T>(serviceMetadata);
    }


}
