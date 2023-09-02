package com.gdou.netty.handler;


import com.gdou.common.message.RpcRequestMessage;
import com.gdou.common.message.RpcResponseMessage;
import com.gdou.netty.config.ServiceAnnotationsScanner;
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
            Class<?> implClazz = ServiceAnnotationsScanner.annotatedClassMap.get(msg.getInterfaceName());
            Object implObj = ServiceAnnotationsScanner.getOrCreateClassObject(implClazz);
            Method method = implClazz.getMethod(msg.getMethodName(), msg.getParameterTypes());
            Object result = method.invoke(implObj, msg.getParameterValue());
            responseMessage.setReturnValue(result);
        } catch (Exception e) {
            e.printStackTrace();
            responseMessage.setExceptionValue(new Exception("远程调用出错：" + e.getCause().getMessage()));
        }
        ctx.writeAndFlush(responseMessage);
    }
}
