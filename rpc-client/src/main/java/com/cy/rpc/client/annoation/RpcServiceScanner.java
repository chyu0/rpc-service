package com.cy.rpc.client.annoation;

import com.cy.rpc.client.proxy.RpcProxyFactoryBean;
import com.cy.rpc.common.annotation.RpcService;
import com.cy.rpc.common.enums.RpcErrorEnum;
import com.cy.rpc.common.exception.RpcException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author chenyu3
 * rpc服务扫描器，扫描包含RPCService注解的类
 */
public class RpcServiceScanner extends ClassPathBeanDefinitionScanner {

    private static final String INTERFACE_CLASS = "interfaceClass";
    private static final String APP_ID = "appId";
    private static final String SERVICE_NAME = "serviceName";

    private final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    public RpcServiceScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    protected void registerFilters() {
        addIncludeFilter(new AnnotationTypeFilter(RpcService.class));
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Assert.notEmpty(basePackages, "At least one base package must be specified");
        Assert.notNull(getRegistry(), "Registry can not by null");
        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
            for (BeanDefinition candidate : candidates) {
                ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
                candidate.setScope(scopeMetadata.getScopeName());

                try {
                    if(StringUtils.isBlank(candidate.getBeanClassName())) {
                        logger.warn("RpcServiceScanner doScan Bean Class Name 为空！" + candidate.toString());
                        continue;
                    }

                    Class<?> clazz = Class.forName(candidate.getBeanClassName());

                    RpcService rpcService = clazz.getAnnotation(RpcService.class);
                    if(rpcService == null) {
                        if(logger.isDebugEnabled()) {
                            logger.debug("类上不包含Rpc Service 注解");
                        }
                        continue;
                    }

                    String[] beanNames;
                    int defaultIndex = 0;
                    String environmentPros = this.getEnvironment().getProperty(candidate.getBeanClassName());
                    if(StringUtils.isNotBlank(environmentPros)) {
                        String[] overrideBean = environmentPros.split(",");
                        Assert.isTrue(overrideBean.length == rpcService.value().length, candidate.getBeanClassName() + "RpcService value()数量不匹配");
                        beanNames = overrideBean;
                    }else {
                        beanNames = rpcService.value();
                        for(int index = 0; index < beanNames.length; index++) {
                            if(rpcService.defaultValue().equals(beanNames[index])) {
                                defaultIndex = index;
                                break;
                            }
                        }
                    }

                    //如果不是ScannedGenericBeanDefinition， 直接取默认值
                    if(!(candidate instanceof ScannedGenericBeanDefinition)) {
                        processBeanDefinition(candidate, beanNames[defaultIndex], rpcService.value()[defaultIndex]);
                        beanDefinitions.add(registryBeanDefinition(candidate, rpcService.defaultValue()));
                        continue;
                    }

                    for(int index = 0; index < beanNames.length; index++) {
                        BeanDefinition beanDefinition = (BeanDefinition)((ScannedGenericBeanDefinition) candidate).clone();
                        processBeanDefinition(beanDefinition, beanNames[index], rpcService.value()[index]);
                        beanDefinitions.add(registryBeanDefinition(beanDefinition, beanNames[index]));
                    }

                }catch (Exception e){
                    logger.error("扫描实例化失败！", e);
                }

            }
        }

//        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            logger.warn("没有找到basePackage:" + Arrays.toString(basePackages));
            return beanDefinitions;
        }

        return beanDefinitions;
    }

    /**
     * 定义bean definition
     * @param definition
     */
    private void processBeanDefinition(BeanDefinition definition, String beanName, String serviceName) throws ClassNotFoundException {

        GenericBeanDefinition genericBeanDefinition = (GenericBeanDefinition)definition;

        String className = definition.getBeanClassName();

        if(StringUtils.isBlank(className)) {
            logger.error(" processBeanDefinitions 异常，找不到appId的配置, className 为空");
            return;
        }
        String appId;
        if(className.contains("MyService")) {
            appId = "rpc-server-test";
        }else {
            appId = "rpc-server2-test";
        }

        definition.getConstructorArgumentValues().addGenericArgumentValue(className);

        if(StringUtils.isBlank(appId)) {
            logger.error("className : "+ className +", processBeanDefinitions 异常，找不到appId的配置");
            return;
        }

        definition.getPropertyValues().add(INTERFACE_CLASS, Class.forName(className));
        definition.getPropertyValues().add(APP_ID, appId);
        definition.getPropertyValues().add(SERVICE_NAME, StringUtils.isBlank(serviceName) ? beanName : serviceName);

        genericBeanDefinition.setBeanClass(RpcProxyFactoryBean.class);
        genericBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

    }

    /**
     * 注册definition holder
     * @param candidate
     * @param beanName
     * @return
     */
    private BeanDefinitionHolder registryBeanDefinition(BeanDefinition candidate, String beanName) {
        Assert.notNull(getRegistry(), "Registry can not by null");
        if (candidate instanceof AbstractBeanDefinition) {
            postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
        }

        if (candidate instanceof AnnotatedBeanDefinition) {
            AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
        }

        if (checkCandidate(beanName, candidate)) {
            BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
            registerBeanDefinition(definitionHolder, getRegistry());
            return definitionHolder;
        }

        throw new RpcException(RpcErrorEnum.INNER_ERROR, "注册bean失败，beanName已重复，beanName : " + beanName);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }



}
