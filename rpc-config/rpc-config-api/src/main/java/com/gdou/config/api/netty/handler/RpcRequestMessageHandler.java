package com.gdou.config.api.netty.handler;


import com.gdou.common.message.RpcRequestMessage;
import com.gdou.common.message.RpcResponseMessage;
import com.gdou.config.api.RpcBootStrap;
import com.gdou.config.api.ServiceConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) {
        RpcResponseMessage responseMessage = new RpcResponseMessage();
        responseMessage.setSequenceId(msg.getSequenceId());
        try {
            ServiceConfig<?> serviceConfig = RpcBootStrap
                    .getInstance()
                    .configManager
                    .getService(msg.getServiceId());
            // 当远程客户端发来请求 获取实现类类型
            Class<?> implClazz = serviceConfig.getMetadata().getServiceType();
            // 调用提前准备好的代理执行
            Object ref = serviceConfig.getRef();
            Method method = implClazz.getMethod(msg.getMethodName(), msg.getParameterTypes());
            Object result = method.invoke(ref, msg.getParameterValue());
            responseMessage.setReturnValue(result);
        } catch (Exception e) {
            e.printStackTrace();
            responseMessage.setExceptionValue(new Exception("远程调用出错：" + e.getCause().getMessage()));
        }
        ctx.writeAndFlush(responseMessage);
    }
}
