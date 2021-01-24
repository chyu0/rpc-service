package com.cy.rpc.client.cluster;

import com.cy.rpc.client.Client;
import com.cy.rpc.client.cluster.selector.AbstractSelector;
import com.cy.rpc.client.cluster.selector.RandomSelector;
import com.cy.rpc.common.enums.RpcErrorEnum;
import com.cy.rpc.common.exception.RpcException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chenyu3
 * 客户端集群，对客户端集群进行管理
 */
@Getter
@Setter
@Slf4j
public class ClientCluster {

    /**
     * 客户端对应的appName
     */
    private String appName;

    /**
     * 该集群下所有的客户端连接
     */
    private Set<Client> clients = Collections.synchronizedSet(new HashSet<>());

    /**
     * 客户端对应的eventGroup
     */
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();;

    /**
     * 客户端对应的选择器
     */
    private AbstractSelector selector;

    /**
     * 初始化，除appName都赋默认值
     * @param appName appName
     */
    public ClientCluster(String appName) {
        this.appName = appName;
        this.selector = new RandomSelector();
    }

    public ClientCluster(String appName, AbstractSelector selector) {
        this.appName = appName;
        this.selector = selector;
    }

    /**
     * 向客户端集群添加一个客户端连接
     * @param remoteAddress 远程地址
     * @param port 端口
     */
    synchronized void addClient(String remoteAddress, int port) {
        boolean contains = clients.stream().anyMatch(item -> item.getRemoteAddress().equals(remoteAddress) && item.getPort() == port);
        if(contains) {
            log.warn("已经包含相同的客户端连接，无需继续添加，remoteAddress : {}, port : {}", remoteAddress, port);
            return ;
        }
        Client client = new Client(appName, remoteAddress, port);
        clients.add(client);
    }

    /**
     * 移除一个客户端，指定某个客户端
     * @param client 客户端连接
     */
    synchronized void removeClient(Client client) {
        boolean contains = clients.stream().noneMatch(item -> client == item);
        if(contains) {
            log.warn("未找到对应的客户端，移除失败！，client : {}", client);
            return ;
        }
        clients.remove(client);
        //关闭socket channel
        client.getSocketChannel().close();
    }

    /**
     * 移除通过远程端口进行移除
     * @param remoteAddress 远程地址
     * @param port 端口
     */
    synchronized void removeClient(String remoteAddress, int port) {
        Client client;
        synchronized (this) {
            client = getClient(remoteAddress, port);
            if(client == null) {
                log.warn("未找到对应的客户端，移除失败！，remoteAddress : {}, port : {}", remoteAddress, port);
                return ;
            }
            clients.remove(client);
        }
        //关闭socket channel
        client.getSocketChannel().close();
    }

    /**
     * 获取一个客户端连接，通过选择器获取
     * @return 客户端连接
     */
    public Client getClient() {
        //过滤掉未激活的客户端，即正在重新连接的客户端
        Set<Client> filterNoActive = clients.stream().
                filter(item -> item.getSocketChannel() != null && item.getSocketChannel().isActive()).collect(Collectors.toSet());

        if(CollectionUtils.isEmpty(filterNoActive)) {
            throw new RpcException(RpcErrorEnum.CLIENT_NOT_FOUND);
        }

        return selector.getClient(filterNoActive);
    }

    /**
     * 是否存在客户端连接
     * @param remoteAddress 远程地址
     * @param port 端口
     * @return 是否存在
     */
    public boolean exist(String remoteAddress, int port) {
        return clients.stream().anyMatch(item -> item.getRemoteAddress().equals(remoteAddress) && item.getPort() == port);
    }

    /**
     * 获取集群下的客户端连接
     * @param remoteAddress 远程地址
     * @param port 端口
     * @return 客户端连接
     */
    public Client getClient(String remoteAddress, int port) {
        Optional<Client> optional = clients.stream().filter(item -> item.getRemoteAddress().equals(remoteAddress) && item.getPort() == port).findFirst();
        if(!optional.isPresent()) {
            log.warn("未找到客户端连接！，remoteAddress : {}, port : {}", remoteAddress, port);
            return null;
        }
        return optional.get();
    }

    /**
     * 通过localAddress获取到对应客户端
     * @param localAddress 本地地址
     * @return 客户端连接
     */
    public Client getClient(String localAddress) {
        Optional<Client> optional = clients.stream().filter(item -> item.getLocalAddress().equals(localAddress)).findFirst();
        if(!optional.isPresent()) {
            log.warn("未找到客户端连接！，localAddress : {}", localAddress);
            return null;
        }
        return optional.get();
    }

    @Override
    public String toString() {
        return  "{ appName = " + appName + " ," +
                "selector = " + selector.getClass().getSimpleName() + " ," +
                "clients = " + clients + " }";
    }
}
