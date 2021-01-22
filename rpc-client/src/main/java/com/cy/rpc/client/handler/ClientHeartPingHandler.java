package com.cy.rpc.client.handler;

import com.cy.rpc.client.Client;
import com.cy.rpc.client.sockect.ClientFactory;
import com.cy.rpc.common.constant.MessageConstant;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author chenyu3
 * 定时向服务端发送心跳
 */
@Slf4j
public class ClientHeartPingHandler extends ChannelInboundHandlerAdapter {

    private final String appName;

    public ClientHeartPingHandler(String appName) {
        this.appName = appName;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //本地地址
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        String address = socketChannel.localAddress().toString();

        List<Client> clients = ClientFactory.get(appName);
        Optional<Client> clientOpt = clients.stream().filter(item -> address.equals(item.getLocalAddress())).findFirst();
        if(clientOpt.isPresent()) {
            Client client = clientOpt.get();
            //重试
            client.tryConnect();
        }else {
            ctx.close();
            ctx.channel().close();
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("(ClientHeartPingHandler)rpc client channel active");
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        socketChannel.eventLoop().scheduleAtFixedRate(() -> {
            if(socketChannel.isActive()) {
                log.info("ClientHeartPingHandler，客户端发送心跳！");
                socketChannel.write(Unpooled.copiedBuffer(MessageConstant.HEART_BEAT.getBytes()));
                socketChannel.writeAndFlush(Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes()));
            }else {
                log.info("ClientHeartPingHandler客户端未激活，发送失败，ctx isRemove:{}", ctx.isRemoved());
                ctx.close();
                ctx.channel().close();
                ctx.channel().eventLoop().shutdownGracefully();
            }
        },0,30, TimeUnit.SECONDS);
        ctx.fireChannelActive();
    }

}
