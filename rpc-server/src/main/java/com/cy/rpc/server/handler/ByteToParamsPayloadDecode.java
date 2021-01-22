package com.cy.rpc.server.handler;

import com.cy.rpc.common.payload.MethodPayload;
import com.cy.rpc.register.utils.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author chenyu3
 * 字节转参数
 */
@Slf4j
public class ByteToParamsPayloadDecode extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        log.info("ByteToParamsPayloadDecode decode: {}" , in.readableBytes());

        if(in.readableBytes() <= 0) {
            log.error("ByteToParamsPayloadDecode没有可读的字节");
            return ;
        }

        int first = in.getInt(0);
        //读到字节大于可读字节数，跳过
        if(first <= 0 || first > in.readableBytes()) {
            out.add(Unpooled.copiedBuffer(in));
            in.skipBytes(in.readableBytes());
            return ;
        }


        if(in.readableBytes() >= in.readInt()) {
            out.add(getParamsPayLoad(in));
        }

    }

    private MethodPayload getParamsPayLoad(ByteBuf in) {
        MethodPayload methodPayload = new MethodPayload();

        //method
        int methodLength = in.readInt();
        byte[] methodBytes = new byte[methodLength];
        in.readBytes(methodBytes, 0, methodLength);
        String method = new String(methodBytes);
        methodPayload.setMethod(method);

        //请求id
        int requestIdLength = in.readInt();
        byte[] requestIdBytes = new byte[requestIdLength];
        in.readBytes(requestIdBytes, 0, requestIdLength);
        String requestId = new String(requestIdBytes);
        methodPayload.setRequestId(requestId);

        //服务名称
        int serviceNameLength = in.readInt();
        byte[] serviceNameBytes = new byte[serviceNameLength];
        in.readBytes(serviceNameBytes, 0, serviceNameLength);
        String serviceName = new String(serviceNameBytes);
        methodPayload.setServiceName(serviceName);

        //参数
        int paramsNum = in.readInt();
        if(paramsNum > 0) {
            Object[] params = new Object[paramsNum];
            for(int index = 0; index < paramsNum; index ++) {
                int arg = in.readInt();
                byte[] argBytes = new byte[arg];
                in.readBytes(argBytes, 0, arg);
                params[index] = ByteUtils.toObject(argBytes);
            }
            methodPayload.setArgs(params);
        }

        //参数类型
        int classNum = in.readInt();
        if(classNum > 0) {
            Class<?>[] argClass = new Class<?>[classNum];
            for(int index = 0; index < classNum; index ++) {
                int arg = in.readInt();
                byte[] argBytes = new byte[arg];
                in.readBytes(argBytes, 0, arg);
                argClass[index] = (Class<?>)ByteUtils.toObject(argBytes);
            }
            methodPayload.setArgsClass(argClass);
        }

        return methodPayload;
    }

}
