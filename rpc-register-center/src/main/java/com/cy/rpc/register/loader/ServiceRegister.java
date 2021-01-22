package com.cy.rpc.register.loader;

import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.framework.ServiceCuratorFramework;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Watcher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author chenyu3
 * ServiceLoader
 * 加载所有文件
 */
@Slf4j
public class ServiceRegister {

    private static final String PREFIX = "META-INF/services/**";

    private static final String SERVICE_INTERFACE_PATH = "/interface";

    private static final String PROVIDER = "provider";

    private static final String CONSUMER = "consumer";

    private static final String SEPARATOR = "/";

    /**
     * 加载所有资源，获取文件名称，及类目
     * @return
     */
    public static void registerProviderInterface(String appName, String ip, int port) {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(ResourceUtils.CLASSPATH_URL_PREFIX + PREFIX);
            ServiceCuratorFramework framework = ZookeeperClientFactory.getDefaultClient();
            ServiceCuratorFramework appFramework = ZookeeperClientFactory.getCuratorFrameworkByAppName(appName);
            for(Resource resource : resources) {
                String providerPath = StringUtils.joinWith(SEPARATOR, SERVICE_INTERFACE_PATH, resource.getFilename(), PROVIDER);
                String oldAppName = framework.get(providerPath, null);
                if(oldAppName == null) {
                    framework.persist(providerPath, appName);
                }else if(!oldAppName.equals(appName)) {
                    log.error("{}接口已存在appName:{}，新appName:{}，忽略", resource.getFilename(), oldAppName, appName);
                    continue;
                }
                //加服务端口临时节点
                appFramework.ephemeral(StringUtils.joinWith(SEPARATOR, providerPath, ip + ":" + port), null);
            }
        }catch (Exception e){
            log.error("加载资源失败！classpath : {}", PREFIX);
        }
    }

    /**
     * 注册消费者接口
     */
    public static void registerConsumerInterface(String interfaceName, String appName, String ip) {
        ServiceCuratorFramework framework = ZookeeperClientFactory.getDefaultClient();
        framework.ephemeral(StringUtils.joinWith(SEPARATOR, SERVICE_INTERFACE_PATH, interfaceName, CONSUMER, appName, ip), null);
    }

    /**
     * 获取接入所有的hostname
     * @param interfaceName
     * @param watcher
     * @return
     */
    public static Map<String, Set<String>> getHostNames(String interfaceName, Watcher watcher) {
        String appNamePath = StringUtils.joinWith(SEPARATOR, SERVICE_INTERFACE_PATH, interfaceName, PROVIDER);

        ServiceCuratorFramework framework = ZookeeperClientFactory.getDefaultClient();
        String appName = framework.get(appNamePath, null);

        if(StringUtils.isBlank(appName)) {
            return Collections.emptyMap();
        }

        ServiceCuratorFramework appNameFrameWork = ZookeeperClientFactory.getCuratorFrameworkByAppName(appName);
        return Collections.singletonMap(appName, new HashSet<>(appNameFrameWork.getChildren(appNamePath, watcher)));
    }

    /**
     * 获取appName
     * @param interfaceName
     * @return
     */
    public static String getAppName(String interfaceName) {
        String appNamePath = StringUtils.joinWith(SEPARATOR, SERVICE_INTERFACE_PATH, interfaceName, PROVIDER);
        ServiceCuratorFramework framework = ZookeeperClientFactory.getDefaultClient();
        return framework.get(appNamePath, null);
    }

}
