package com.cy.rpc.register.loader;

import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.framework.ServiceCuratorFramework;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author chenyu3
 * ServiceLoader
 * 加载所有文件
 */
@Slf4j
public class ServerServiceRegister {

    private static final String PREFIX = "META-INF/services/**";

    private static final String SERVICE_INTERFACE_PATH = "/interface/";

    private static final String SERVER_PATH = "/server/";

    /**
     * 加载所有资源，获取文件名称，及类目
     * @return
     */
    public static void registerInterface(String appName) {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(ResourceUtils.CLASSPATH_URL_PREFIX + PREFIX);
            ServiceCuratorFramework framework = ZookeeperClientFactory.getDefaultClient();
            for(Resource resource : resources) {
                String path = SERVICE_INTERFACE_PATH + resource.getFilename();

                framework.persist(path, null);
                framework.ephemeral(path + "/" + appName, null);

                framework.getChildren(path, new Watcher() {
                    @Override
                    public void process(WatchedEvent watchedEvent) {
                        if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                            //循环注册
                            List<String> nodes = framework.getChildren(path, this);
                            if(StringUtils.isEmpty(nodes)) {
                                framework.remove(path);
                            }
                        }
                    }
                });
            }
        }catch (Exception e){
            log.error("加载资源失败！classpath : {}", PREFIX);
        }
    }

    /**
     * 注册服务
     */
    public static void registerServer(String appName, int port) {
        String path = SERVER_PATH +  appName;
        ServiceCuratorFramework framework = ZookeeperClientFactory.getCuratorFrameworkByAppName(appName);
        framework.persist(path, null);
        framework.ephemeral(path + "/" + "127.0.0.1:" + port, null);

        framework.getChildren(path, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                    //循环注册
                    List<String> nodes = framework.getChildren(path, this);
                    if(StringUtils.isEmpty(nodes)) {
                        framework.remove(path);
                    }
                }
            }
        });
    }

}
