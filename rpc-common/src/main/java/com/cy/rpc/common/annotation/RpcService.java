package com.cy.rpc.common.annotation;

import java.lang.annotation.*;

/**
 * @author chenyu3
 * 有此注解表示是Rpc Service，作用于接口
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface RpcService {

    String[] value();

    String defaultValue();

}
