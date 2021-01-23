package com.cy.rpc.client.cluster.selector;

import com.cy.rpc.client.Client;

import java.util.Set;

/**
 * @author chenyu3
 * 轮询选择器
 */
public class PollingSelector extends AbstractSelector {

    //当前轮询的索引
    private volatile int index = 0;

    @Override
    public synchronized Client getClient(Set<Client> clients) {
        if(index >= clients.size()) {
            index = 0;
        }
        //轮询策略，如果index超出范围，则清0
        return (Client) clients.toArray()[index++];
    }
}
