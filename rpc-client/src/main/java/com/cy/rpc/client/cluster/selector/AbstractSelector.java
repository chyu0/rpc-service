package com.cy.rpc.client.cluster.selector;

import com.cy.rpc.client.Client;

import java.util.Set;

/**
 * @author chenyu3
 * 获取客户端策略
 */
public abstract class AbstractSelector {

    public abstract Client getClient(Set<Client> clients);
}
