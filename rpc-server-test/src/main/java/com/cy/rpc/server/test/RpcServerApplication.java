package com.cy.rpc.server.test;

import com.cy.rpc.common.annotation.EnableRpcService;
import com.cy.rpc.common.enums.RpcMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRpcService(mode = RpcMode.SERVER)
public class RpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcServerApplication.class, args);
    }

}
