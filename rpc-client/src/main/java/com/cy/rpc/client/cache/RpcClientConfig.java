package com.cy.rpc.client.cache;

import com.cy.rpc.client.cluster.selector.AbstractSelector;
import com.cy.rpc.client.cluster.selector.ClientSelectorEnum;
import com.cy.rpc.client.cluster.selector.RandomSelector;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.EnumUtils;

/**
 * @author chenyu3
 * rpc客户端基本配置缓存，缓存基本配置信息
 */
@Getter
@Setter
public class RpcClientConfig {

    /**
     * 接口调用超时时间
     */
    private long timeout;

    /**
     * 选择器
     */
    private AbstractSelector selector;

    /**
     * 无参构造函数
     */
    public RpcClientConfig() {
        Builder builder = new Builder();
        this.timeout = builder.timeout;
        this.selector = builder.selector;
    }

    /**
     * 构造函数
     * @param builder
     */
    public RpcClientConfig(Builder builder) {
        this.timeout = builder.timeout;
        this.selector = builder.selector;
    }

    /**
     * builder
     */
    @Getter
    @Setter
    public static class Builder {
        /**
         * 接口调用超时时间
         */
        private long timeout = 3000;

        /**
         * 选择器
         */
        private AbstractSelector selector = new RandomSelector();

        /**
         * 设置开始时间
         * @param timeout
         * @return
         */
        public Builder timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder selector(String selector) {
            this.selector = EnumUtils.getEnum(ClientSelectorEnum.class, selector).getSelector();
            return this;
        }

        public RpcClientConfig build() {
            return new RpcClientConfig(this);
        }
    }

    /**
     *
     * @return
     */
    public static Builder builder() {
        return new Builder();
    }

}
