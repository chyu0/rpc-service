package com.cy.rpc.register;

/**
 * @author chenyu3
 * 注册中心策略
 */
public interface RegisterStrategy {

    void init();

    void close();

    String get(String path);

    boolean isExisted(String path);

    void persist(String path, String value);

    void update(String path, String value);

    void remove(String path);

}
