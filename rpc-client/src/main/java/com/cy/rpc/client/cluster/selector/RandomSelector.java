package com.cy.rpc.client.cluster.selector;

import com.cy.rpc.client.Client;

import java.util.Random;
import java.util.Set;

/**
 * @author chenyu3
 * 随机获取客户端
 */
public class RandomSelector extends AbstractSelector {

    @Override
    public Client getClient(Set<Client> clients) {
        //负载均衡到一条服务器，后期加策略
        int randomIndex = new Random().nextInt(clients.size());
        return (Client) clients.toArray()[randomIndex];
    }
}
