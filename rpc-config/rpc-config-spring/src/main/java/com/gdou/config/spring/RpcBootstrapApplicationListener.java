package com.gdou.config.spring;

import com.gdou.config.api.RpcBootStrap;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class RpcBootstrapApplicationListener implements ApplicationListener {

    public RpcBootstrapApplicationListener() {
        this.rpcBootStrap = RpcBootStrap.getInstance();
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        }
    }

    public static final String BEAN_NAME = "rpcBootstrapApplicationListener";

    private final RpcBootStrap rpcBootStrap;

    private void onContextRefreshedEvent(ContextRefreshedEvent event) {
        rpcBootStrap.start();
    }

}