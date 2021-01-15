package com.cy.rpc.register.framework;

import com.google.common.base.Charsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author chenyu3
 * zookeeper基本操作接口
 */
@Slf4j
public class ServiceCuratorFramework {

    @Getter
    private final CuratorFramework client;

    @Getter
    private final List<ACL> aclList;

    public ServiceCuratorFramework(CuratorFramework client, List<ACL> aclList) {
        this.client = client;
        this.aclList = aclList;
    }

    public ServiceCuratorFramework(CuratorFramework client) {
        this.client = client;
        this.aclList = null;
    }

    /**
     * 获取节点数据
     * @param path
     * @return
     */
    public String get(String path, Watcher watcher) {
        try {
            GetDataBuilder builder = client.getData();
            if(watcher != null) {
                builder.usingWatcher(watcher);
            }
            byte[] bytes = builder.forPath(path);
            return new String(bytes);
        } catch (Exception e) {
            log.error("ServiceCuratorFramework查询zk节点失败！path:{}", path, e);
            return null;
        }
    }

    /**
     * 节点是否存在
     * @param path
     * @return
     */
    public boolean existed(String path, Watcher watcher) {
        try {
            ExistsBuilder existsBuilder = client.checkExists();
            if(watcher != null) {
                existsBuilder.usingWatcher(watcher);
            }
            Stat stat = existsBuilder.forPath(path);
            return stat != null;
        } catch (Exception e) {
            log.error("ServiceCuratorFramework判断zk节点是否失败异常！path:{}", path, e);
            return false;
        }
    }

    /**
     * 持久化节点
     * @param path
     * @param value
     */
    public void persist(String path, String value) {
        try {
            if (!existed(path, null)) {
                ACLBackgroundPathAndBytesable<String> aclMode = client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT);
                //创建时设置节点权限
                if(!CollectionUtils.isEmpty(aclList)) {
                    aclMode.withACL(aclList);
                }
                aclMode.forPath(path, value != null ? value.getBytes(Charsets.UTF_8) : null);
            } else {
                update(path, value);
            }
        } catch (Exception e) {
            log.error("ServiceCuratorFramework节点持久化失败！path:{}", path, e);
        }
    }

    /**
     * 临时节点
     * @param path
     * @param value
     */
    public void ephemeral(String path, String value) {
        try {
            if (!existed(path, null)) {
                ACLBackgroundPathAndBytesable<String> aclMode = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL);
                //创建时设置节点权限
                if(!CollectionUtils.isEmpty(aclList)) {
                    aclMode.withACL(aclList);
                }
                aclMode.forPath(path, value != null ? value.getBytes(Charsets.UTF_8) : null);
            } else {
                update(path, value);
            }
        } catch (Exception e) {
            log.error("ServiceCuratorFramework节点持久化失败！path:{}", path, e);
        }
    }

    /**
     * 获取所有子节点
     * @param path
     * @return
     */
    public List<String> getChildren(String path, Watcher watcher) {
        try {
            GetChildrenBuilder builder = client.getChildren();
            if(watcher != null) {
                builder.usingWatcher(watcher);
            }
            return builder.forPath(path);
        }catch (Exception e){
            log.error("ServiceCuratorFramework获取子节点失败！path:{}, value:{}", path, e);
            return Collections.emptyList();
        }
    }


    /**
     * 更新
     * @param path
     * @param value
     */
    public void update(String path, String value) {
        try {
            client.inTransaction().check().forPath(path).and().setData().forPath(path, value != null ? value.getBytes(Charsets.UTF_8) : null).and().commit();
        }catch (Exception e){
            log.error("ServiceCuratorFramework更新节点失败！path:{}, value:{}", path, value, e);
        }
    }

    /**
     * 删除
     * @param path
     */
    public void remove(String path) {
        try {
            client.delete().forPath(path);
        }catch (Exception e){
            log.error("ServiceCuratorFramework删除节点失败！path:{}", path, e);
        }
    }

}
