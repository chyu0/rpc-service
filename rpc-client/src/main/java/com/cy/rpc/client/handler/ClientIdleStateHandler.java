package com.cy.rpc.client.handler;

import com.cy.rpc.common.constant.MessageConstant;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chenyu3
 * 主要是客户端心跳，超时未写，超时未读事件，服务端连接断开事件
 */
@Slf4j
public class ClientIdleStateHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        try {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if (e.state() == IdleState.WRITER_IDLE) {
                    log.info("长时间都没有写数据，自动发送心跳进行检测");
                    ctx.channel().write(Unpooled.copiedBuffer(MessageConstant.HEART_BEAT.getBytes()));
                    ctx.channel().writeAndFlush(Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes()));
                }else if (e.state() == IdleState.READER_IDLE) {
                    log.info("长时间都没有读到服务端返回的数据，与服务断开连接");
                    ctx.disconnect();
                    ctx.channel().disconnect();
                }
            } else if (evt instanceof ChannelInputShutdownEvent) {
                log.info("远程连接已关闭！断开客户端连接，准备开始重连");
                //远程主机强制关闭连接
                ctx.channel().disconnect();
            }
        } catch (Exception e) {
            log.error("监听事件出现异常", e);
        }
    }
}
