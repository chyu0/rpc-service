package com.cy.rpc.logger.handler;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * encode
 */
public class ILoggingEventEncoder extends MessageToByteEncoder<ILoggingEvent> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ILoggingEvent iLoggingEvent, ByteBuf byteBuf){
         byteBuf.writeBytes(Unpooled.copiedBuffer(iLoggingEvent.getMessage().getBytes()));
    }
}
