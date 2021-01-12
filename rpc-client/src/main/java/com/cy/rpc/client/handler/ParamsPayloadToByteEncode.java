package com.cy.rpc.client.handler;

import com.cy.rpc.common.payload.MethodPayload;
import com.cy.rpc.common.utils.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chenyu3
 * 编码器，paramsPayLoad -> byte[]
 */
@Slf4j
public class ParamsPayloadToByteEncode extends MessageToByteEncoder<MethodPayload> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MethodPayload payload, ByteBuf out){

        ByteBuf byteBuf = Unpooled.directBuffer(1024 * 10);

        String methodName = payload.getMethod();
        byteBuf.writeInt(methodName.length());
        byteBuf.writeBytes(methodName.getBytes());

        String requestId = payload.getRequestId();
        byteBuf.writeInt(requestId.length());
        byteBuf.writeBytes(requestId.getBytes());

        String serviceName = payload.getServiceName();
        byteBuf.writeInt(serviceName.length());
        byteBuf.writeBytes(serviceName.getBytes());

        if(payload.getArgs() != null && payload.getArgs().length > 0) {
            //设置参数个数
            byteBuf.writeInt(payload.getArgs().length);
            for(Object object : payload.getArgs()) {
                byte[] param = ByteUtils.toByteArray(object);
                byteBuf.writeInt(param.length);
                byteBuf.writeBytes(param);
            }
        }else {
            byteBuf.writeInt(0);
        }

        if(payload.getArgsClass() != null && payload.getArgsClass().length > 0) {
            //设置参数个数
            byteBuf.writeInt(payload.getArgsClass().length);
            for(Class<?> object : payload.getArgsClass()) {
                byte[] clazz = ByteUtils.toByteArray(object);
                byteBuf.writeInt(clazz.length);
                byteBuf.writeBytes(clazz);
            }
        }else {
            byteBuf.writeInt(0);
        }


        log.info("ParamsPayloadToByteEncode encode" + byteBuf.readableBytes() + " " + System.currentTimeMillis());
        out.writeInt(byteBuf.readableBytes());
        out.writeBytes(byteBuf);

    }

}
