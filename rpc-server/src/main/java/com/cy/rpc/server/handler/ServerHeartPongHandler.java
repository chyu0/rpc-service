package com.cy.rpc.server.handler;

import com.cy.rpc.common.constant.MessageConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * @author chenyu3
 * 心跳监测
 */
@Slf4j
public class ServerHeartPongHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.error("服务端与客户端断开连接，ctx channel : {}", ctx.channel());
        ctx.close();
        ctx.channel().close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        String message = byteBuf.toString(Charset.defaultCharset());
        log.info("channelRead0收到心跳数据：{}" , message);
        //context不触发下一个handler，直接发送给客户端收到消息
        ctx.channel().write(Unpooled.copiedBuffer(MessageConstant.RECEIVE.getBytes()));
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes()));
    }

}
