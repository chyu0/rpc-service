package com.cy.rpc.client.proxy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.util.CastUtils;

import java.lang.reflect.Proxy;

/**
 * @author chenyu3
 * bean实例化工厂类，单例
 * @param <T>
 */
@Getter
@Setter
public class RpcProxyFactoryBean<T> implements FactoryBean<T> {

    /**
     * 接口类型
     */
    private Class<T> interfaceClass;

    /**
     * 本地地址
     */
    private String appId;

    /**
     * 服务名称
     */
    private String serviceName;


    public RpcProxyFactoryBean(Class<T> interfaceType) {
        this.interfaceClass = interfaceType;
    }

    @Override
    public T getObject() {
        return CastUtils.cast(Proxy.newProxyInstance(DefaultProxy.class.getClassLoader(),
                new Class<?>[]{interfaceClass}, new DefaultProxy(appId, serviceName)));
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
