package com.cy.rpc.register.framework;

import com.cy.rpc.register.RegisterStrategy;
import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.exception.ZkConnectException;
import com.cy.rpc.register.exception.ZkErrorEnum;
import com.cy.rpc.register.properties.ZookeeperProperties;
import com.cy.rpc.register.utils.Base64Utils;
import com.google.common.base.Charsets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chenyu3
 *
 */
@Getter
@Setter
@Slf4j
public class ZookeeperRegisterStrategy implements RegisterStrategy {

    private ZookeeperProperties properties;

    private CuratorFramework client;

    private String appName;

    private List<ACL> createNodeACLs;

    public ZookeeperRegisterStrategy(ZookeeperProperties properties, String appName) {
        this.properties = properties;
        this.appName = appName;
    }

    @Override
    public void init() {
        client = ZookeeperClientFactory.getClients().get(appName);
        if(client == null) {
            log.error("appName未找到！appName:{}", appName);
            throw new ZkConnectException(ZkErrorEnum.APP_NAME_NOT_FOUND);
        }

        //设置创建节点acl
        if(StringUtils.isNotBlank(properties.getZkDigest())) {
            if(createNodeACLs == null) {
                createNodeACLs = new ArrayList<>();
            }
            createNodeACLs.add(buildAllACL(properties.getZkDigest()));
        }

        //设置创建节点acl
        if(StringUtils.isNotBlank(properties.getCreateNodeUserAndPsd())) {
            if(createNodeACLs == null) {
                createNodeACLs = new ArrayList<>();
            }
            createNodeACLs.add(buildReadOnlyACL(properties.getCreateNodeUserAndPsd()));
        }

    }

    @Override
    public void close() {
        //是否可以直接关闭
        boolean flag = true;

        for(Map.Entry<String, CuratorFramework> framework : ZookeeperClientFactory.getClients().entrySet()) {
            //client有其他appName在使用，不可以删除
            if(!framework.getKey().equals(appName) && framework.getValue() == client) {
                flag = false;
                break;
            }
        }
        //先移除appName
        ZookeeperClientFactory.getClients().remove(appName);
        if(flag) {
            CloseableUtils.closeQuietly(client);
        }
    }

    @Override
    public String get(String path) {
        try {
            byte[] bytes = client.getData().forPath(path);
            return new String(bytes);
        } catch (Exception e) {
            log.error("查询zk节点失败！path:{}", path, e);
            return null;
        }
    }

    @Override
    public boolean isExisted(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);
            return stat != null;
        } catch (Exception e) {
            log.error("判断zk节点是否失败异常！path:{}", path, e);
            return false;
        }
    }

    @Override
    public void persist(String path, String value) {
        try {
            if (!isExisted(path)) {
                ACLBackgroundPathAndBytesable<String> aclMode = client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT);
                //创建时设置节点权限
                if(!CollectionUtils.isEmpty(createNodeACLs)) {
                    aclMode.withACL(createNodeACLs);
                }
                aclMode.forPath(path, value.getBytes(Charsets.UTF_8));
            } else {
                update(path, value);
            }
        } catch (Exception e) {
            log.error("节点持久化失败！path:{}", path, e);
        }
    }

    @Override
    public void update(String path, String value) {
        try {
            client.inTransaction().check().forPath(path).and().setData().forPath(path, value.getBytes(Charsets.UTF_8)).and().commit();
        }catch (Exception e){
            log.error("更新节点失败！path:{}, value:{}", path, value, e);
        }
    }

    @Override
    public void remove(String path) {
        try {
            client.delete().forPath(path);
        }catch (Exception e){
            log.error("删除节点失败！path:{}", path, e);
        }
    }


    /**
     * 构建一个权限控制对象，只包含读，针对客户端
     * @return
     */
    public static ACL buildReadOnlyACL(String digest) {
        if(StringUtils.isBlank(digest)) {
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
    public static ACL buildAllACL(String zkDigest) {
        if(StringUtils.isBlank(zkDigest)) {
            return null;
        }

        String userName = zkDigest.split(":")[0];
        String base64Digest = Base64Utils.getDigest(zkDigest);

        return new ACL(ZooDefs.Perms.ALL, new Id("digest", userName + ":" + base64Digest));
    }
}
