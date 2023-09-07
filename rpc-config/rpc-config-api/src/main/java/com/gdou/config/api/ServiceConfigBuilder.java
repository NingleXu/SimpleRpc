package com.gdou.config.api;

import com.gdou.common.config.metadata.ServiceMetadata;

/**
 * @author ningle
 * @version : ServiceConfigBuilder.java, v 0.1 2023/09/06 12:53 ningle
 **/
public class ServiceConfigBuilder<U> {

    private String version;

    private String group;

    private U ref;
    private Class<U> serviceType;

    public static ServiceConfigBuilder newBuilder() {
        return new ServiceConfigBuilder();
    }

    public ServiceConfigBuilder<U> version(String version) {
        this.version = version;
        return this;
    }

    public ServiceConfigBuilder<U> group(String group) {
        this.group = group;
        return this;
    }

    public ServiceConfigBuilder<U> interfaceClass(Class<U> interfaceClazz) {
        this.serviceType = interfaceClazz;
        return this;
    }

    public ServiceConfigBuilder<U> ref(U ref) {
        this.ref = ref;
        return this;
    }


    public ServiceConfig<U> build() {
        ServiceMetadata serviceMetadata = new ServiceMetadata(this.version, this.group, this.serviceType);
        return new ServiceConfig<U>(serviceMetadata, ref);
    }
}
