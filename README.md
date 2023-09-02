# SimpleRpc

基于Java开发的RPC远程调用框架，使用Netty实现网络通信，Jdk动态代理，支持配置注册中心、负载均衡策略等其它功能。
功能较为简单，但是足够体现出RPC的调用大致流程，适合RPC初学者了解远程调用框架执行流程和原理学习。

## 现有功能

- 基于Netty实现的网络通信，其中具有自定义通信协议和序列化方式等可自定义的内容。
- 仿Dubbo的SPI机制实现，Dubbo的SPI对Java原生SPI机制上进行了增强，赋予了加载扩展类依赖注入IOC、AOP等功能，并且支持指定名称查找对应的实现类。SimpleRpc也实现了基于服务名称去查找对应的实现类的功能，但功能较为简单，可供学习。
- 支持Nacos注册中心，服务端自动扫描@RpcService注解，并将服务注册到注册中心。客户端可依据负载均衡策略获取到具体的服务提供者。目前仅支持随机策略。
- 基于配置文件的扩展性实现，可以通过更改配置文件选择特定功能的实现。

## 模块介绍

### rpc-common

工具类模块，在此可以拓展序列化方式、自定义注解和消息通讯格式等功能。

### rpc-netty

netty网络通信模块，主要分为服务器端和客户端实现，负责网络通信协议的对齐和数据的分发。

### rpc-register

注册中心模块，这里主要有不同注册中心的实现和对应的负载均衡策略

### rpc-test

测试与使用模块

### rpc-core

核心模块，定义了客户端和服务器端的入口

## 如何使用？

1. 下载源代码

2.  maven 仓库对rpc-core执行install操作

![image](https://github.com/Xzhhhhhh/SimpleRpc/assets/91795546/26bcd878-d50c-4d97-8da1-2ec904d1b638)


​	3. 在其它项目中引入依赖

```maven
    <dependency>
            <groupId>com.gdou</groupId>
            <artifactId>rpc-core</artifactId>
            <version>0.0.1</version>
    </dependency>
```



4. 编码

   新建三个模块 consumer、provider、api，consumer和provider模块中引入上述依赖和api模块的依赖， 在api模块定义通用接口。

   api模块

![image](https://github.com/Xzhhhhhh/SimpleRpc/assets/91795546/52109cbf-5359-43b1-b177-daeb8b438d9e)


```java
public interface HelloService {

    String sayHello(String name);

}
```

​	provider 模块

![image](https://github.com/Xzhhhhhh/SimpleRpc/assets/91795546/ac21119c-d3e9-4cd5-8d8e-ddbae1abfcd5)


```java
public class ProviderApplication {
    public static void main(String[] args) {
        RpcServerBootStrap.getInstance()
                .registry(new InetSocketAddress("localhost", 8848))
                .start()
                .await();
    }
}
```



consumer 模块
![image](https://github.com/Xzhhhhhh/SimpleRpc/assets/91795546/faed2a16-b372-4eb5-8667-a26e03efd26d)

```java
public class ConsumerApplication {
    public static void main(String[] args) {
        RpcClientBootStrap client = RpcClientBootStrap.getInstance()
                .registry(new InetSocketAddress("localhost", 8848));

        HelloService helloService = client.getProxy(HelloService.class);

        System.out.println(helloService.sayHello("SimpleRpc"));
    }
}
```

5. consumer 和 provider的resources目录下下编写配置文件 rpc-config.properties

   如果不写也可以，都有默认值

   ```properties
   serializer.algorithm=json
   scan.packages=com.gdou
   ```

5. 启动nacos

6. 启动服务端和客户端查看调用结果

![image](https://github.com/Xzhhhhhh/SimpleRpc/assets/91795546/9d228dc2-e19f-45b6-8360-c06eef543b09)
