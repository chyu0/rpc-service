package com.cy.rpc.server.handler;

import com.cy.rpc.common.payload.ResultPayload;
import com.cy.rpc.register.utils.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chenyu3
 * 结果转字节byte[]
 */
@Slf4j
public class ResultPayloadToByteEncode extends MessageToByteEncoder<ResultPayload> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ResultPayload msg, ByteBuf out) {

        ByteBuf byteBuf = Unpooled.directBuffer(1024 * 10);
        byteBuf.writeInt(msg.getRequestId().length()).writeBytes(msg.getRequestId().getBytes());

        byteBuf.writeInt(msg.getCode());
        byteBuf.writeBoolean(msg.isSuccess());

        byte[] messageBytes = msg.getMessage().getBytes();
        byteBuf.writeInt(messageBytes.length).writeBytes(messageBytes);

        byte[] result = ByteUtils.toByteArray(msg.getResult());
        byteBuf.writeInt(result.length).writeBytes(result);

        log.info("ResultPayloadToByteEncode encode" + byteBuf.readableBytes());
        out.writeInt(byteBuf.readableBytes());
        out.writeBytes(byteBuf);

    }
}
