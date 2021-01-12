package com.cy.rpc.server.service;

/**
 * @author chenyu3
 * 获取service抽象工厂，比如通过bean获取
 */
public abstract class AbstractServiceFactory {

    /**
     * 通过服务名称获取
     * @param serviceName
     * @return
     */
    public abstract Object getServiceByName(String serviceName);
}
