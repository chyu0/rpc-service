package com.cy.rpc.logger.handler;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * encode
 */
public class ILoggingEventEncoder extends MessageToMessageEncoder<ILoggingEvent> {

    private InetSocketAddress remoteAddress;

    public ILoggingEventEncoder(InetSocketAddress remoteAddress){
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ILoggingEvent msg, List<Object> out) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(msg.toString().getBytes());
        out.add(new DatagramPacket(byteBuf, remoteAddress));
    }
}
