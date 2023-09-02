package com.gdou.netty.protocol;

import com.gdou.common.message.Message;
import com.gdou.common.serializer.SerializerType;
import com.gdou.common.utils.RpcConfigReader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.gdou.common.constants.RpcConfigConstants.DEFAULT_SERIALIZER;
import static com.gdou.common.constants.RpcConfigConstants.SERIALIZER_KEY;

@Slf4j
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {

    private static final String serializerType;

    static {
        serializerType = RpcConfigReader.getConfigProperty(SERIALIZER_KEY, DEFAULT_SERIALIZER);
        log.debug("SimpleRpc 当前序列化方式 :" + serializerType);
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Message message, List<Object> list) throws Exception {

        ByteBuf byteBuf = ctx.alloc().buffer();

        // 字节的魔数
        byteBuf.writeBytes(new byte[]{'n', 'i', 'n', 'g', 'l', 'e', 0, 2});
        // 字节的版本
        byteBuf.writeByte(1);

        // 字节的序列化方式 jdk 0 json 1
        byteBuf.writeByte(SerializerType.valueOf(serializerType).getCode());
        // 消息类型
        byteBuf.writeByte(message.getMessageType());
        // 请求序号
        byteBuf.writeInt(message.getSequenceId());

        // 对齐填充
        byteBuf.writeByte(0xff);

        // 获取对象字节数组
        byte[] bytes = SerializerType.valueOf(serializerType).serializer(message);
        // 长度
        byteBuf.writeInt(bytes.length);
        // 内容
        byteBuf.writeBytes(bytes);

        list.add(byteBuf);
    }

    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 魔术
        ByteBuf magicNumber = byteBuf.readBytes(8);
        // 版本
        byte version = byteBuf.readByte();
        // 序列化方式
        byte serializerType = byteBuf.readByte();
        // 指令类型
        byte messageType = byteBuf.readByte();
        // 请求序号
        int sequenceId = byteBuf.readInt();

        // 对齐填充
        byte ignore = byteBuf.readByte();

        // 内容长度
        int contentLength = byteBuf.readInt();
        // 消息体
        byte[] bytes = new byte[contentLength];
        byteBuf.readBytes(bytes, 0, contentLength);

        //确定具体的消息类型
        Class<? extends Message> messageClazz = Message.getMessageClass(messageType);
        Message message = SerializerType.values()[serializerType].deserializer(messageClazz, bytes);
        // 网络通信日志
//        log.debug("magicNumber: {},version: {},serializerType: {},messageType: {}, sequenceId: {}, contentLength: {}",
//                magicNumber, version, serializerType, messageType, sequenceId, contentLength);
//        log.debug("content: {}", message);
        list.add(message);
    }


}
