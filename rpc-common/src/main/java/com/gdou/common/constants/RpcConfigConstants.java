package com.gdou.common.constants;

/**
 * @author ningle
 * @version : RpcConfigConstants.java, v 0.1 2023/08/31 23:55 ningle
 * 框架配置常量
 **/
public interface RpcConfigConstants {

    String SERIALIZER_KEY = "serializer.algorithm";
    String DEFAULT_SERIALIZER = "jdk";

    String SERVER_PORT = "server.port";

    int DEFAULT_SERVER_PORT = 8080;

    String LOADBALANCE_KEY = "loadbalance";

    String DEFAULT_LOADBALANCE = "random";

    String SCAN_PACKAGES_KEY = "scan.packages";
    String DEFAULT_SCAN_PACKAGES = "";

    String SPI_PATH = "META-INFO/rpc/";

    String REGISTRY_KEY = "registry";
    String DEFAULT_REGISTRY = "nacos";



}
