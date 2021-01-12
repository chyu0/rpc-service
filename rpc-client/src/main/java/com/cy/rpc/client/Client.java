package com.cy.rpc.client;

import com.cy.rpc.client.handler.*;
import com.cy.rpc.client.sockect.ClientFactory;
import com.cy.rpc.common.constant.MessageConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
@Slf4j
public class Client {

    private final String remoteAddress;

    private final int port;

    private final String appId;

    private SocketChannel socketChannel;

    private String localAddress;

    public Client(String appId, String remoteAddress, int port){
        this.appId = appId;
        this.remoteAddress = remoteAddress;
        this.port = port;
    }

    public void connect() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast(new IdleStateHandler(60, 60, 0, TimeUnit.SECONDS))
                                    .addLast(new DelimiterBasedFrameDecoder(1024 * 10, Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes())))
                                    .addLast(new ParamsPayloadToByteEncode())
                                    .addLast(new ByteToResultPayloadDecode())
                                    .addLast(new ClientHeartPingHandler(appId))
                                    .addLast(new ClientIdleStateHandler())
                                    .addLast(new ClientHeartPongHandler())
                                    .addLast(new RpcClientChannelHandler());

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
            localAddress = socketChannel.localAddress().toString();
            ClientFactory.put(appId, this);
            socketChannel.config().setAllowHalfClosure(true);

        }catch (Exception e){
            group.shutdownGracefully();
            tryConnect();
        }
    }

    /**
     * 重连
     */
    public void tryConnect() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.schedule(() -> {
            connect();
            log.info("连接服务端！address: {}, address: {}, port: {}", appId, remoteAddress, port);
        }, 10, TimeUnit.SECONDS);
    }

}
