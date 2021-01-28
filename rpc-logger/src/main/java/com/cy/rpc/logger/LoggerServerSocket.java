package com.cy.rpc.logger;

import com.cy.rpc.logger.handler.RpcServerChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

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

    public ServerSocketChannel start() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port)).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline()
                        .addLast(new DelimiterBasedFrameDecoder(1024 * 10, Unpooled.copiedBuffer(FINISH.getBytes())))
                        .addLast(new StringDecoder())
                        .addLast(new RpcServerChannelHandler())
                ;
            }
        });
        return (ServerSocketChannel) bootstrap.bind().sync().channel();
    }


    public static void main(String[] args) throws Exception {
        LoggerServerSocket socket = new LoggerServerSocket(1111);
        socket.start();
    }
}
