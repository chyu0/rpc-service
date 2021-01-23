package com.cy.rpc.client.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenyu3
 * 客户端集群工厂类
 * 主要对集群进行管理
 */
@Slf4j
public class ClientClusterCache {

    private static final Map<String, ClientCluster> clusterMap = new ConcurrentHashMap<>(new HashMap<>());

    /**
     * 返回一个客户端集群
     * @param appName app名称
     * @return 客户端集群
     */
    public static ClientCluster getCluster(String appName) {
        return clusterMap.get(appName);
    }

    /**
     * 写入客户端集群，并打开客户端连接
     * @param appName app名称
     * @param remoteAddress 远程地址
     * @param port 端口
     */
    public static synchronized ClientCluster put(String appName, String remoteAddress, int port) {
        ClientCluster cluster = clusterMap.computeIfAbsent(appName, k -> new ClientCluster(appName));
        cluster.addClient(remoteAddress, port);
        return cluster;
    }



    /**
     * 移除一个客户端连接，放到ClientCluster重连中进行管理，如果连接失败的话，就需要移除client
     * @param appName app名称
     * @param remoteAddress 远程地址
     * @param port 端口
     */
    public static synchronized void remove(String appName, String remoteAddress, int port) {
        ClientCluster clientCluster = clusterMap.get(appName);
        if(clientCluster != null) {
            clientCluster.removeClient(remoteAddress, port);
            //如果客户端连接数量为0，就关闭group连接
            if(clientCluster.getClients().size() == 0) {
                clientCluster.getEventLoopGroup().shutdownGracefully();
                clusterMap.remove(appName);
            }
        }
    }

}
