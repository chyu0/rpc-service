package com.cy.rpc.register.curator;

import com.cy.rpc.register.framework.ServiceCuratorFramework;
import com.cy.rpc.register.properties.RpcServiceZookeeperProperties;
import com.cy.rpc.register.utils.Base64Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author chenyu3
 * zk客户端连接工厂
 */
@Slf4j
public class ZookeeperClientFactory {

    @Getter
    private static final Map<String, ServiceCuratorFramework> clients = new ConcurrentHashMap<>(new HashMap<>());

    @Getter
    private static ServiceCuratorFramework defaultClient = null;
    /**
     * 初始化
     * @param properties
     */
    public static void init(RpcServiceZookeeperProperties properties) {
        //默认连接
        if(defaultClient == null) {
            defaultClient = new ServiceCuratorFramework(builder(properties).build());
            defaultClient.getClient().start();
        }

        //服务端
        if(clients.get(properties.getAppName()) == null) {
            //设置访问权限
            if(StringUtils.hasText(properties.getZkDigest())) {
                List<ACL> aclList = new ArrayList<>();
                aclList.add(buildAllACL(properties.getZkDigest()));
                if(StringUtils.hasText(properties.getCreateNodeUserAndPsd())) {
                     aclList.add(buildReadOnlyACL(properties.getCreateNodeUserAndPsd()));
                }
                clients.put(properties.getAppName(), new ServiceCuratorFramework(buildServer(properties), aclList));
            }else {
                clients.put(properties.getAppName(), defaultClient);
            }
        }

        //客户端
        if(!CollectionUtils.isEmpty(properties.getDigestMap())) {
            for(Map.Entry<String, String> appEntry : properties.getDigestMap().entrySet()) {
                //未添加过
                if(clients.get(appEntry.getKey()) == null) {
                    clients.put(appEntry.getKey(), new ServiceCuratorFramework(buildClient(properties, appEntry.getValue())));
                }
            }
        }

        //启动所有zk连接
        for(ServiceCuratorFramework framework : clients.values()) {
            if(framework.getClient().getZookeeperClient().isConnected()) {
                continue;
            }

            //启动
            framework.getClient().start();
            try {
                framework.getClient().blockUntilConnected(properties.getMaxSleepTimeMilliseconds() * properties.getMaxRetries(), TimeUnit.MILLISECONDS);
                if(!framework.getClient().getZookeeperClient().isConnected()) {
                    CloseableUtils.closeQuietly(framework.getClient().getZookeeperClient());
                }
                log.info("Client success to connect to zookeeper service");
            } catch (final Exception ex) {
                log.error("Client failed to connect to zookeeper service : ", ex);
            }
        }
    }

    public static void close() {
        for(Map.Entry<String, ServiceCuratorFramework> appEntry : clients.entrySet()) {
            CloseableUtils.closeQuietly(appEntry.getValue().getClient());
        }
        clients.clear();
    }


    //获取默认客户端
    public static ServiceCuratorFramework getCuratorFrameworkByAppName(String appName) {
        return clients.get(appName) != null ? clients.get(appName) : defaultClient;
    }

    /**
     * 带权限验证
     * @return
     */
    public static CuratorFramework buildServer(RpcServiceZookeeperProperties properties) {
        CuratorFrameworkFactory.Builder builder = builder(properties);
        if(!StringUtils.hasText(properties.getZkDigest())) {
            return builder.build();
        }

        return builder.aclProvider(buildDefaultAclProvider()).authorization("digest", properties.getZkDigest().getBytes()).build();
    }

    /**
     * 带权限验证
     * @return
     */
    private static CuratorFramework buildClient(RpcServiceZookeeperProperties properties, String digest) {
        CuratorFrameworkFactory.Builder builder = builder(properties);
        if(!StringUtils.hasText(digest)) {
            return builder.build();
        }

        if(CollectionUtils.isEmpty(properties.getDigestMap())) {
            return builder.build();
        }

        return builder.aclProvider(buildDefaultAclProvider()).authorization("digest", digest.getBytes()).build();
    }

    /**
     * 构建一个基本的 curator framework builder对象
     * @return
     */
    private static CuratorFrameworkFactory.Builder builder(RpcServiceZookeeperProperties properties) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder.connectString(properties.getServerLists()).namespace(properties.getNamespace())
                .retryPolicy(new ExponentialBackoffRetry(properties.getBaseSleepTimeMilliseconds(), properties.getMaxRetries(), properties.getMaxSleepTimeMilliseconds()));

        if(properties.getSessionTimeoutMilliseconds() > 0) {
            builder.sessionTimeoutMs(properties.getSessionTimeoutMilliseconds());
        }

        if(properties.getConnectionTimeoutMilliseconds() > 0) {
            builder.connectionTimeoutMs(properties.getConnectionTimeoutMilliseconds());
        }
        return builder;
    }

    private static ACLProvider buildDefaultAclProvider() {
        return new ACLProvider() {
            @Override
            public List<ACL> getDefaultAcl() {
                return ZooDefs.Ids.CREATOR_ALL_ACL;
            }

            @Override
            public List<ACL> getAclForPath(String s) {
                return ZooDefs.Ids.CREATOR_ALL_ACL;
            }
        };
    }


    /**
     * 构建一个权限控制对象，只包含读，针对客户端
     * @return
     */
    private static ACL buildReadOnlyACL(String digest) {
        if(org.apache.commons.lang3.StringUtils.isBlank(digest)) {
            return null;
        }

        String userName = digest.split(":")[0];
        String base64Digest = Base64Utils.getDigest(digest);

        return new ACL(ZooDefs.Perms.READ, new Id("digest", userName + ":" + base64Digest));
    }

    /**
     * 构建一个权限控制对象，包含读，写，创建的权限，digest是加密后的令牌，作用于服务端
     * @return
     */
    private static ACL buildAllACL(String zkDigest) {
        if(org.apache.commons.lang3.StringUtils.isBlank(zkDigest)) {
            return null;
        }

        String userName = zkDigest.split(":")[0];
        String base64Digest = Base64Utils.getDigest(zkDigest);

        return new ACL(ZooDefs.Perms.ALL, new Id("digest", userName + ":" + base64Digest));
    }
}
