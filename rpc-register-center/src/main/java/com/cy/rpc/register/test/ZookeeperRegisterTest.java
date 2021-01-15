package com.cy.rpc.register.test;

import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.framework.ServiceCuratorFramework;
import com.cy.rpc.register.properties.RpcServiceZookeeperProperties;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class ZookeeperRegisterTest {

    public static void main(String[] args) throws InterruptedException {
        RpcServiceZookeeperProperties properties = new RpcServiceZookeeperProperties();
        //服务端appName
        properties.setAppName("server-test-1");
        //服务端创建节点时，都会设置用户digest，假设他创捷节点的digest是test:123456
        properties.setCreateNodeUserAndPsd("test:123456");
        properties.setZkDigest("zkDigest:123456");
        //clientAppNames为空就表示没有需要连接的客户端
        ZookeeperClientFactory.init(properties);

        //获取server-test-1的CuratorFramework
        ServiceCuratorFramework serverFramework = ZookeeperClientFactory.getCuratorFrameworkByAppName("server-test-1");

        List<String> clients = serverFramework.getChildren("/server", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                    List<String> nodes = serverFramework.getChildren("/server", this);
                    System.out.println("---------------------------------------->" + nodes);
                    if(CollectionUtils.isEmpty(nodes)) {
                        serverFramework.remove("/server");
                    }
                }
            }
        });

        System.out.println(clients);

        //为server-test-1设置创建节点，同时都会设置digest，为test:123456只读，zkDigest下用户有所有权限
        serverFramework.persist("/server", properties.getAppName());
        serverFramework.persist("/server/node1", "120.0.0.1:8080");
        serverFramework.persist("/server/node2", "120.0.0.1:8081");



        RpcServiceZookeeperProperties clientProperties = new RpcServiceZookeeperProperties();
        //客户端appName
        clientProperties.setAppName("client-server-test-1");
        //服务端创建节点时，会设置用户digest，为client:123456，他创建节点时这个为只读
        clientProperties.setCreateNodeUserAndPsd("client:123456");
        Map<String, String> digestMap = new HashMap<>();
        //一个客户端client，其实就是对应的server-test-1，别名叫client
        digestMap.put("client", "test:123456");
        clientProperties.setDigestMap(digestMap);

        List<String> clientList = new ArrayList<>();
        //这个client，就是上面的client别名，用户初始化
        clientList.add("client");
        ZookeeperClientFactory.init(clientProperties);


        ServiceCuratorFramework clientServerFramework = ZookeeperClientFactory.getCuratorFrameworkByAppName("client-server-test-1");
        clientServerFramework.persist("/server2", properties.getAppName());
        clientServerFramework.persist("/server2/node1", "120.0.0.1:8080");
        clientServerFramework.persist("/server2/node2", "120.0.0.1:8081");

        //获取client的CuratorFramework，也就是别名为client，digest是test:123456，所以就是对应到上面服务端的server-test-1，当然有权限获取节点信息啦
        ServiceCuratorFramework clientFramework = ZookeeperClientFactory.getCuratorFrameworkByAppName("client");

        //客户端获取值
        System.out.println(clientFramework.get("/server", null));
        System.out.println(clientFramework.get("/server/node1", null));
        System.out.println(clientFramework.get("/server/node2", null));

        //当然会报错啦，客户端没有删除的权限啦
        clientFramework.remove("/server/node1");

        serverFramework.remove("/server/node1");
        serverFramework.remove("/server/node2");

    }
}
