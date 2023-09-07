package com.gdou.common.config.metadata;

/**
 * @author ningle
 * @version : ServiceMetadata.java, v 0.1 2023/09/06 10:40 ningle
 **/
public class ServiceMetadata {

    private String version;

    private String group;

    private Class<?> serviceType;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Class<?> getServiceType() {
        return serviceType;
    }

    public void setServiceType(Class<?> serviceType) {
        this.serviceType = serviceType;
    }

    public ServiceMetadata(String version, String group, Class<?> serviceType) {
        this.version = version;
        this.group = group;
        this.serviceType = serviceType;
    }
}
