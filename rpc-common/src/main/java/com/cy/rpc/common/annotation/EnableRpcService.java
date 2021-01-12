package com.cy.rpc.common.annotation;

import com.cy.rpc.common.enums.RpcMode;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RpcServiceConfigurationSelector.class)
public @interface EnableRpcService {

    RpcMode mode() default RpcMode.ALL;

}
