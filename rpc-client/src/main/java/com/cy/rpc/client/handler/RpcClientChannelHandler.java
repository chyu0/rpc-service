package com.cy.rpc.client.handler;

import com.cy.rpc.client.future.FutureFactory;
import com.cy.rpc.common.payload.ResultPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chenyu3
 * 处理结果返回
 */
@Getter
@Slf4j
public class RpcClientChannelHandler extends SimpleChannelInboundHandler<ResultPayload> {

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, ResultPayload resultPayload) {
        channelHandlerContext.channel().eventLoop().execute(() -> {
            FutureFactory.receive(resultPayload.getRequestId(), resultPayload);
        });
    }

}
