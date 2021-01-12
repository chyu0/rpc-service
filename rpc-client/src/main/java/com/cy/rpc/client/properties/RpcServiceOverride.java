package com.cy.rpc.client.properties;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author chenyu3
 * service bean name重写，考虑到重复bean情况，没有重复bean name无需处理
 */
@Getter
@Setter
public class RpcServiceOverride implements Serializable {

    private String[] values;

    private String defaultValue;
}
