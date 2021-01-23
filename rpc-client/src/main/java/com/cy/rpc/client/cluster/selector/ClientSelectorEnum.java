package com.cy.rpc.client.cluster.selector;

import lombok.Getter;

/**
 * @author chenyu3
 * 客户端选择器枚举
 */
public enum ClientSelectorEnum {

    RandomSelector(new RandomSelector(), "随机选择器"),
    PollingSelector(new PollingSelector(), "轮询选择器");

    ClientSelectorEnum(AbstractSelector selector, String desc) {
        this.selector = selector;
        this.desc = desc;
    }

    @Getter
    private AbstractSelector selector;

    @Getter
    private String desc;


}
