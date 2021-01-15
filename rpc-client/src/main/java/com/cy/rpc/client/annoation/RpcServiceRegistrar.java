package com.cy.rpc.client.annoation;

import com.cy.rpc.register.curator.ZookeeperClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author chenyu3
 * rpc扫描注册器
 */
@Slf4j
public class RpcServiceRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;

    private Environment environment;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(RpcClientScan.class.getName()));

        if(annoAttrs == null) {
            log.info("注解未找到！RpcClientScan");
            return ;
        }

        String[] scanPackages = annoAttrs.getStringArray("basePackages");

        RpcServiceScanner scanner = new RpcServiceScanner(registry);
        scanner.setResourceLoader(resourceLoader);
        scanner.setEnvironment(environment);

        //过滤RpcService注解
        scanner.registerFilters();

        scanner.doScan(scanPackages);

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
