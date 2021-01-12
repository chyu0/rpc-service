package com.cy.rpc.client.annoation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author chenyu3
 * rpc scan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(RpcServiceRegistrar.class)
public @interface RpcClientScan {

    String[] basePackages() default {};
}
