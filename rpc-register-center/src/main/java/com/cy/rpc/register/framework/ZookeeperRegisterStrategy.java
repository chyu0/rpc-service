package com.cy.rpc.register.framework;

import com.cy.rpc.register.RegisterStrategy;
import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.exception.ZkConnectException;
import com.cy.rpc.register.exception.ZkErrorEnum;
import com.cy.rpc.register.properties.ZookeeperProperties;
import com.google.common.base.Charsets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

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
            log.error("查询zk节点失败！path:{}", path);
            return null;
        }
    }

    @Override
    public boolean isExisted(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);
            return stat != null;
        } catch (Exception e) {
            log.error("判断zk节点是否失败异常！path:{}", path);
            return false;
        }
    }

    @Override
    public void persist(String path, String value) {
        try {
            if (!isExisted(path)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, value.getBytes(Charsets.UTF_8));
            } else {
                update(path, value);
            }
        } catch (Exception ex) {
            log.error("节点持久化失败！path:{}", path);
        }
    }

    @Override
    public void update(String path, String value) {
        try {
            client.inTransaction().check().forPath(path).and().setData().forPath(path, value.getBytes(Charsets.UTF_8)).and().commit();
        }catch (Exception e){
            log.error("更新节点失败！path:{}, value:{}", path, value);
        }
    }

    @Override
    public void remove(String path) {
        try {
            client.delete().forPath(path);
        }catch (Exception e){
            log.error("删除节点失败！path:{}", path);
        }
    }
}
