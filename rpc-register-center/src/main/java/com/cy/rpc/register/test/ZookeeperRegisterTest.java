package com.cy.rpc.register.test;

import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.framework.ZookeeperRegisterStrategy;
import com.cy.rpc.register.properties.ZookeeperProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ZookeeperRegisterTest {

    public static void main(String[] args) {
        ZookeeperProperties properties = new ZookeeperProperties();
        //服务端appName
        properties.setAppName("server-test-1");
        //服务端创建节点时，都会设置用户digest，假设他创捷节点的digest是test:123456
        properties.setCreateNodeUserAndPsd("test:123456");
        properties.setZkDigest("zkDigest:123456");
        //clientAppNames为空就表示没有需要连接的客户端
        ZookeeperClientFactory.init(properties, null);

        //获取server-test-1的CuratorFramework
        ZookeeperRegisterStrategy strategyStragegy = new ZookeeperRegisterStrategy(properties, "server-test-1");
        strategyStragegy.init();

        //为server-test-1设置创建节点，同时都会设置digest，为test:123456只读，zkDigest下用户有所有权限
        strategyStragegy.persist("/server", properties.getAppName());
        strategyStragegy.persist("/server/node1", "120.0.0.1:8080");
        strategyStragegy.persist("/server/node2", "120.0.0.1:8081");


        ZookeeperProperties clientProperties = new ZookeeperProperties();
        //客户端appName
        clientProperties.setAppName("client-server-test-1");
        //服务端创建节点时，会设置用户digest，为client:123456，他创建节点时这个为只读
        clientProperties.setCreateNodeUserAndPsd("client:123456");
        Map<String, String> digestMap = new HashMap<>();
        //一个客户端client，其实就是对应的server-test-1，别名叫client
        digestMap.put("client", "test:123456");
        clientProperties.setDigestMap(digestMap);
        //这个client，就是上面的client别名，用户初始化
        ZookeeperClientFactory.init(clientProperties, Collections.singletonList("client"));

        ZookeeperRegisterStrategy clientWriteStrategy = new ZookeeperRegisterStrategy(clientProperties, "client-server-test-1");
        clientWriteStrategy.init();
        clientWriteStrategy.persist("/server2", properties.getAppName());
        strategyStragegy.persist("/server2/node1", "120.0.0.1:8080");
        strategyStragegy.persist("/server2/node2", "120.0.0.1:8081");

        //获取client的CuratorFramework，也就是别名为client，digest是test:123456，所以就是对应到上面服务端的server-test-1，当然有权限获取节点信息啦
        ZookeeperRegisterStrategy clientStrategy = new ZookeeperRegisterStrategy(clientProperties, "client");
        clientStrategy.init();

        //客户端获取值
        System.out.println(clientStrategy.get("/server"));
        System.out.println(clientStrategy.get("/server/node1"));
        System.out.println(clientStrategy.get("/server/node2"));

        //当然会报错啦，客户端没有删除的权限啦
        clientStrategy.remove("/server/node1");
    }
}
