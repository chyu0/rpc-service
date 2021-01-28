package com.cy.rpc.client.handler;

import com.cy.rpc.client.cluster.ClientCluster;
import com.cy.rpc.client.cluster.ClientClusterCache;
import com.cy.rpc.client.cluster.ClientConnector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chenyu3
 * 定时向服务端发送心跳
 */
@Slf4j
public class ClientInactiveHandler extends ChannelInboundHandlerAdapter {

    private final String appName;

    public ClientInactiveHandler(String appName) {
        this.appName = appName;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //先通过appName获取集群配置，找到那台机器进行连接
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        String localAddress = socketChannel.localAddress().toString();
        log.info("客户端连接已失活，localAddress：{}", localAddress);
        ClientCluster cluster = ClientClusterCache.getCluster(appName);
        if(cluster != null) {
            log.info("客户端准备重新连接到服务端，localAddress：{}", localAddress);
            ClientConnector.tryConnect(appName, localAddress);
        }else {
            log.info("没有客户端集群，无法重连，客户端连接直接关闭，localAddress：{}", localAddress);
            ctx.close();
            ctx.channel().close();
        }
    }

}
