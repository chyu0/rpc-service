package com.cy.rpc.client.cluster;

import com.cy.rpc.client.Client;
import com.cy.rpc.client.cache.ServiceCache;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chenyu3
 * 对客户端进行连接
 */
@Slf4j
public class ClientConnect {

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    /**
     * 连接客户端
     * @param appName app名称
     * @param remoteAddress 远程连接
     * @param port 端口
     */
    public static void connect(String appName, String remoteAddress, int port) {
        ClientCluster cluster = ClientClusterCache.put(appName, remoteAddress, port);
        ServiceCache.putAppCaches(appName, StringUtils.joinWith(":",  remoteAddress, port));
        //获取集群客户端
        Client client = cluster.getClient(remoteAddress, port);
        //启动客户端连接
        if(!client.connect(cluster.getEventLoopGroup())) {
            log.info("客户端连接失败，准备重试！appName: {}, client: {}", appName, client);
            executeTryConnect(cluster.getEventLoopGroup(), appName, client);
        }
    }


    /**
     * 进行客户端重新连接
     * @param remoteAddress 远程地址
     * @param port 端口
     */
    public static void tryConnect(String appName, String remoteAddress, int port) {
        ClientCluster cluster = ClientClusterCache.getCluster(appName);
        if(cluster == null) {
            log.warn("未找到客户端集群，重连失败！，appName: {}， remoteAddress : {}, port : {}", appName, remoteAddress, port);
            return ;
        }

        Client client = cluster.getClient(remoteAddress, port);
        if(client == null) {
            log.warn("未找到客户端连接，重连失败！，remoteAddress : {}, port : {}", remoteAddress, port);
            return ;
        }
        //客户端重连
        executeTryConnect(cluster.getEventLoopGroup(), appName, client);
    }

    /**
     * 重试客户端连接
     * @param localAddress 客户端地址
     */
    public static void tryConnect(String appName, String localAddress) {
        ClientCluster cluster = ClientClusterCache.getCluster(appName);
        if(cluster == null) {
            log.warn("未找到客户端集群，重连失败！，appName : {}, localAddress : {}", appName, localAddress);
            return ;
        }

        Client client = cluster.getClient(localAddress);
        if(client == null) {
            log.warn("未找到客户端连接，重连失败！，localAddress : {}", localAddress);
            return ;
        }
        //客户端重连
        executeTryConnect(cluster.getEventLoopGroup(), appName, client);
    }

    /**
     * 客户端重试
     * @param client 客户端连接
     */
    private static void executeTryConnect(EventLoopGroup eventLoopGroup, String appName, Client client) {
        //超过最大重试次数的话，就把这个客户端移除掉，重试的目的一个是为了避免网络波动，服务端短暂下线的情况
        if(client.getConnectTimes() >= 3) {
            log.error("连接超时，断开连接，client:{}" , client);
            //移除该客户端的集群配置
            ClientClusterCache.remove(appName, client.getRemoteAddress(), client.getPort());
            ServiceCache.removeAppCache(appName, StringUtils.joinWith(":", client.getRemoteAddress(), client.getPort()));
            return ;
        }

        //默认每10秒重试一次
        scheduledExecutorService.schedule(() -> {
            log.info("客户端重连开始！appName: {}, client: {}", appName, client.toString());
            if(!client.connect(eventLoopGroup)) {
                log.warn("客户端重连失败{}次！appName: {}, client: {}", client.getConnectTimes(), appName, client);
                executeTryConnect(eventLoopGroup, appName, client);
            }
            log.info("客户端重连结束！appName: {}, client: {}", appName, client.toString());
        }, 10, TimeUnit.SECONDS);
    }

}
