package com.cy.rpc.client.test;

import com.cy.rpc.client.annoation.RpcClientScan;
import com.cy.rpc.common.annotation.EnableRpcService;
import com.cy.rpc.common.enums.RpcMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRpcService(mode = RpcMode.ALL)
@RpcClientScan(basePackages = {"com.cy.*"})
public class RpcClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcClientApplication.class, args);
    }

}
