package com.cy.rpc.common.annotation;

import com.cy.rpc.common.enums.RpcErrorEnum;
import com.cy.rpc.common.enums.RpcMode;
import com.cy.rpc.common.exception.RpcException;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenyu3
 * rpc 服务开启 ，可参考EnableCache
 */
public class RpcServiceConfigurationSelector implements ImportSelector {

    private static final String RPC_SERVER_CONFIGURATION_CLASS = "com.cy.rpc.server.configuration.RpcServerConfiguration";

    private static final String RPC_CLIENT_CONFIGURATION_CLASS = "com.cy.rpc.client.configuration.RpcClientConfiguration";

    private static final String RPC_CLIENT_CONSTANT_CONFIGURATION_CLASS = "com.cy.rpc.client.configuration.RpcClientConstantConfiguration";

    public static final String DEFAULT_ATTRIBUTE_NAME = "mode";


    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRpcService.class.getName()));

        if(annotationAttributes == null) {
            throw new RpcException(RpcErrorEnum.INNER_ERROR, "EnableRpcService 启动异常！");
        }

        RpcMode rpcMode = annotationAttributes.getEnum(DEFAULT_ATTRIBUTE_NAME);

        switch (rpcMode) {
            case CLIENT:
                return getClientImports();
            case SERVER:
                return getServerImports();
            case ALL:
                return getAllImports();
            default:
                return null;
        }
    }


    /**
     * 客户端需要引入的类
     * @return
     */
    private String[] getServerImports() {
        List<String> result = new ArrayList<>();
        result.add(RPC_SERVER_CONFIGURATION_CLASS);
        return StringUtils.toStringArray(result);
    }



    /**
     * 客户端需要引入的类
     * @return
     */
    private String[] getClientImports() {
        List<String> result = new ArrayList<>();
        result.add(RPC_CLIENT_CONFIGURATION_CLASS);
        result.add(RPC_CLIENT_CONSTANT_CONFIGURATION_CLASS);
        return StringUtils.toStringArray(result);
    }

    /**
     * 获取所有导入
     * @return
     */
    private String[] getAllImports() {
        String[] clientImports = getClientImports();
        String[] serverImports = getServerImports();
        return StringUtils.concatenateStringArrays(clientImports, serverImports);
    }

}
