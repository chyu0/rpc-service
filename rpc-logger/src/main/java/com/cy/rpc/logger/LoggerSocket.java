package com.cy.rpc.logger;

import com.cy.rpc.logger.handler.ILoggingEventEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Getter
@Setter
@Slf4j
public class LoggerSocket {

    private static final String FINISH = "$FINISH$";

    private NioDatagramChannel socketChannel;

    private final String remoteAddress;

    private final int port;

    public LoggerSocket(String remoteAddress, int port){
        this.remoteAddress = remoteAddress;
        this.port = port;
    }

    /**
     * 连接
     * @return
     */
    public boolean connect() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioDatagramChannel.class)
                .handler(new ILoggingEventEncoder(new InetSocketAddress(remoteAddress, port)));
        bootstrap.option(ChannelOption.SO_BROADCAST, true);

        this.socketChannel = (NioDatagramChannel) bootstrap.bind(0).sync().channel();
        return socketChannel != null && socketChannel.isActive();
    }

}
