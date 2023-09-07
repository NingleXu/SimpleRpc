package com.gdou.common.config;

import com.gdou.common.config.metadata.ServiceMetadata;

/**
 * @author ningle
 * @version : ConfigNameGenerator.java, v 0.1 2023/09/07 13:49 ningle
 **/
public class ConfigNameGenerator {

    private static final String SPLIT = "-";
    private static final String LABEL = "RPC-";
    private static final String LABEL_REFERENCE = "RPC_REFERENCE-";
    private static final String LABEL_SERVICE = "RPC_SERVICE-";

    /**
     * 生成config name
     *
     * @param serviceMetadata config 元数据
     * @return 生成的名称
     */
    public static String generaConfigName(ServiceMetadata serviceMetadata) {
        StringBuilder nameBuilder = new StringBuilder(LABEL);
        if (serviceMetadata.getServiceType() == null) {
            throw new RuntimeException("config service type is not be null");
        }
        // clazz name
        nameBuilder.append(serviceMetadata.getServiceType().getName()).append(SPLIT);

        // version
        if (serviceMetadata.getVersion() != null && !serviceMetadata.getVersion().isEmpty()) {
            nameBuilder.append(serviceMetadata.getVersion()).append(SPLIT);
        }
        // group
        if (serviceMetadata.getGroup() != null && !serviceMetadata.getGroup().isEmpty()) {
            nameBuilder.append(serviceMetadata.getGroup()).append(SPLIT);
        }

        return nameBuilder.deleteCharAt(nameBuilder.length() - 1).toString();
    }


    public static String generaReferenceConfigName(Class<?> serviceClazz, String version, String group) {
        StringBuilder nameBuilder = new StringBuilder(LABEL_REFERENCE);
        if (serviceClazz == null) {
            throw new RuntimeException("config service type is not be null");
        }
        // clazz name
        nameBuilder.append(serviceClazz.getName()).append(SPLIT);

        // version
        if (version != null && !version.isEmpty()) {
            nameBuilder.append(version).append(SPLIT);
        }
        // group
        if (group != null && !group.isEmpty()) {
            nameBuilder.append(group).append(SPLIT);
        }

        return nameBuilder.deleteCharAt(nameBuilder.length() - 1).toString();
    }

    public static String generaServiceConfigName(Class<?> serviceClazz, String version, String group) {
        StringBuilder nameBuilder = new StringBuilder(LABEL_SERVICE);
        if (serviceClazz == null) {
            throw new RuntimeException("config service type is not be null");
        }
        // clazz name
        nameBuilder.append(serviceClazz.getName()).append(SPLIT);

        // version
        if (version != null && !version.isEmpty()) {
            nameBuilder.append(version).append(SPLIT);
        }
        // group
        if (group != null && !group.isEmpty()) {
            nameBuilder.append(group).append(SPLIT);
        }

        return nameBuilder.deleteCharAt(nameBuilder.length() - 1).toString();
    }

}
