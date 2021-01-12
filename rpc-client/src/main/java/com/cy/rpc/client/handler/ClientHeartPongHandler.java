package com.cy.rpc.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * @author chenyu3
 * 心跳监测
 */
@Slf4j
public class ClientHeartPongHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        String message = byteBuf.toString(Charset.defaultCharset());
        log.info("收到心跳数据：{}" , message);
    }

}
