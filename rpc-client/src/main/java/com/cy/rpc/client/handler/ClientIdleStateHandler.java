package com.cy.rpc.client.handler;

import com.cy.rpc.common.constant.MessageConstant;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientIdleStateHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        try {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if (e.state() == IdleState.WRITER_IDLE) {
                    log.info("ClientIdleStateHandler 超时写事件！自动发送心跳");
                    ctx.channel().write(Unpooled.copiedBuffer(MessageConstant.HEART_BEAT.getBytes()));
                    ctx.channel().writeAndFlush(Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes()));
                }else if (e.state() == IdleState.READER_IDLE) {
                    log.info("ClientIdleStateHandler 超时读事件，与服务断开连接");
                    ctx.disconnect();
                    ctx.channel().disconnect();
                }
            } else if (evt instanceof ChannelInputShutdownEvent) {
                log.info("ClientIdleStateHandler 关闭连接！自动发送心跳");
                //远程主机强制关闭连接
                ctx.channel().disconnect();
            }
        } catch (Exception e) {
            log.error("ClientIdleStateHandler 监听事件异常", e);
        }
    }
}
