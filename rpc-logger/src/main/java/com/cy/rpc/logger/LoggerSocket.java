package com.cy.rpc.logger;

import com.cy.rpc.logger.handler.ILoggingEventEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Getter
@Setter
@Slf4j
public class LoggerSocket {

    private static final String FINISH = "$FINISH$";

    private SocketChannel socketChannel;

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
        bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast(new DelimiterBasedFrameDecoder(1024 * 10, Unpooled.copiedBuffer(FINISH.getBytes())))
                                .addLast(new StringEncoder())
                                .addLast(new ILoggingEventEncoder());

                    }
                }).remoteAddress(new InetSocketAddress(remoteAddress, port));
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_SNDBUF, 2*1024)
                .option(ChannelOption.SO_RCVBUF, 2*1024)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,30000);


        this.socketChannel = (SocketChannel) bootstrap.connect().sync().channel();
        if(socketChannel == null) {
            throw new RuntimeException();
        }
        socketChannel.config().setAllowHalfClosure(true);
        return socketChannel != null && socketChannel.isActive();
    }

}
