package com.cy.rpc.server;

import com.cy.rpc.common.constant.MessageConstant;
import com.cy.rpc.server.handler.ByteToParamsPayloadDecode;
import com.cy.rpc.server.handler.ResultPayloadToByteEncode;
import com.cy.rpc.server.handler.RpcServerChannelHandler;
import com.cy.rpc.server.handler.ServerHeartPongHandler;
import com.cy.rpc.server.service.AbstractServiceFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author chenyu3
 * 服务端
 */
@Slf4j
public class Server {

    private final int port;

    private final AbstractServiceFactory factory;

    public Server(AbstractServiceFactory factory, int port){
        this.port = port;
        this.factory = factory;
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
                        .addLast(new DelimiterBasedFrameDecoder(1024 * 10, Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes())))
                        .addLast(new ByteToParamsPayloadDecode())
                        .addLast(new ResultPayloadToByteEncode())
                        .addLast(new ServerHeartPongHandler())
                        .addLast(new RpcServerChannelHandler(factory));
            }
        });
        return (ServerSocketChannel) bootstrap.bind().sync().channel();
    }

}
