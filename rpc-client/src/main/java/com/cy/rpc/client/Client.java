package com.cy.rpc.client;

import com.cy.rpc.client.handler.*;
import com.cy.rpc.common.constant.MessageConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author chenyu3
 * 客户端对象，专门用于进行客户端连接
 */
@Getter
@Slf4j
public class Client {

    private final String remoteAddress;

    private final int port;

    private final String appName;

    private SocketChannel socketChannel;

    private String localAddress;

    private int connectTimes = 0;

    public Client(String appName, String remoteAddress, int port){
        this.appName = appName;
        this.remoteAddress = remoteAddress;
        this.port = port;
    }
    /**
     * 实际连接的方法
     * @param group 组
     * @return 是否连接成功
     */
    public boolean connect(EventLoopGroup group) {
        if(socketChannel != null && socketChannel.isActive()) {
            log.info("连接已经建立成功！remoteAddress:{}, port:{}, localAddress:{}", remoteAddress, port, localAddress);
            return true;
        }

        //连接次数+1
        connectTimes ++;
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
                                    .addLast(new ClientHeartPingHandler(appName))
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
            socketChannel.config().setAllowHalfClosure(true);
            //如果连接成功，连接次数清0
            connectTimes = 0;
        }catch (Exception e){
            log.error("客户端连接失败！client: {}", this, e);
        }
        return socketChannel != null && socketChannel.isActive();
    }

    @Override
    public String toString() {
        return  "{ appName = " + appName + " ," +
                "remoteAddress = " + remoteAddress + " ," +
                "port = " + port + " ," +
                "localAddress = " + localAddress + " ," +
                "connectTimes = " + connectTimes + " ," +
                "isActive = " + (socketChannel != null && socketChannel.isActive()) + " }";
    }
}
