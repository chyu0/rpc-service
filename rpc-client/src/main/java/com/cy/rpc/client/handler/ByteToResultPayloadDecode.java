package com.cy.rpc.client.handler;

import com.cy.rpc.common.payload.ResultPayload;
import com.cy.rpc.register.utils.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author chenyu3
 * 返回结果消息解码器
 */
@Slf4j
public class ByteToResultPayloadDecode extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

        log.info("ByteToResultPayloadDecode decode" + in.readableBytes());

        if(in.readableBytes() <= 0) {
            log.error("ByteToResultPayloadDecode没有可读的字节");
            return ;
        }

        int first = in.getInt(0);
        //读到字节大于可读字节数，跳过，发送到下一个解码器，并跳过字节
        if(first <= 0 || first > in.readableBytes()) {
            out.add(Unpooled.copiedBuffer(in));
            in.skipBytes(in.readableBytes());
            return ;
        }

        if(in.readableBytes() > 0 && in.readableBytes() >= in.readInt()) {
            //请求id
            int requestLen = in.readInt();
            byte[] requestBytes = new byte[requestLen];
            in.readBytes(requestBytes, 0, requestLen);
            String requestId = new String(requestBytes);

            int code = in.readInt();
            boolean success = in.readBoolean();

            int messageLen = in.readInt();
            byte[] messageBytes = new byte[messageLen];
            in.readBytes(messageBytes, 0, messageLen);
            String message = new String(messageBytes);

            int resultLength = in.readInt();
            byte[] resultBytes = new byte[resultLength];
            in.readBytes(resultBytes, 0, resultLength);
            Object result = ByteUtils.toObject(resultBytes);

            ResultPayload resultPayload = new ResultPayload();
            resultPayload.setRequestId(requestId);
            resultPayload.setCode(code);
            resultPayload.setSuccess(success);
            resultPayload.setMessage(message);
            resultPayload.setResult(result);

            out.add(resultPayload);
        }
    }
}
