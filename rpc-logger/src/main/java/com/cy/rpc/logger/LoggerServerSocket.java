package com.cy.rpc.logger;

import com.cy.rpc.logger.handler.ILoggingEventDecoder;
import com.cy.rpc.logger.handler.RpcServerChannelHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * @author chenyu3
 * 日志server socket
 */
public class LoggerServerSocket {

    private static final String FINISH = "$FINISH$";

    private final int port;

    public LoggerServerSocket(int port){
        this.port = port;
    }

    public NioDatagramChannel start() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group).channel(NioDatagramChannel.class)
                .localAddress(new InetSocketAddress(port)).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline()
                        .addLast(new ILoggingEventDecoder())
                        .addLast(new RpcServerChannelHandler());
            }
        });
        bootstrap.option(ChannelOption.SO_BROADCAST, true);
        return (NioDatagramChannel) bootstrap.bind().sync().channel();
    }


    public static void main(String[] args) throws Exception {
        LoggerServerSocket socket = new LoggerServerSocket(1111);
        socket.start();
    }
}
