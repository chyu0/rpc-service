package com.cy.rpc.client.proxy;

import com.cy.rpc.client.Client;
import com.cy.rpc.client.cache.ConfigCache;
import com.cy.rpc.client.cluster.ClientCluster;
import com.cy.rpc.client.cluster.ClientClusterCache;
import com.cy.rpc.client.future.FutureFactory;
import com.cy.rpc.client.future.ResultFuture;
import com.cy.rpc.common.annotation.RpcService;
import com.cy.rpc.common.constant.MessageConstant;
import com.cy.rpc.common.enums.RpcErrorEnum;
import com.cy.rpc.common.exception.RpcException;
import com.cy.rpc.common.payload.MethodPayload;
import com.cy.rpc.common.payload.ResultPayload;
import com.cy.rpc.register.loader.ServiceRegister;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author chenyu3
 * 默认代理实现，向服务端发送消息
 */
@Getter
@Setter
@Slf4j
public class JdkInvocationProxy implements InvocationHandler {

    /**
     * 服务调用名称
     */
    private String serviceName;

    public JdkInvocationProxy(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args){

        Class<?> clazz = method.getDeclaringClass();

        log.info("method: {}, clazz: {}", method.getName(), clazz);
        //从注册中心获取class类对应的appName
        String appName = ServiceRegister.getAppName(clazz.getName());
        //通过appName获取客户端集群
        ClientCluster cluster = ClientClusterCache.getCluster(appName);

        if(cluster == null) {
            throw new RpcException(RpcErrorEnum.CLIENT_CLUSTER_NOT_FOUND, appName + "未找到对应集群客户端");
        }

        log.info("appName : {}, class ： {}, cluster : {}", appName, clazz.getName(), cluster);
        //从集群客户端获取一个client
        Client client = cluster.getClient();
        if(client == null) {
            throw new RpcException(RpcErrorEnum.REMOTE_CONNECTION_CLOSED, "远程连接已关闭" + appName);
        }

        RpcService rpcService = method.getDeclaringClass().getAnnotation(RpcService.class);
        if(rpcService == null) {
            throw new RpcException(RpcErrorEnum.PRC_SERVICE_IS_NULL);
        }

        String[] serviceNames = rpcService.value();

        if(StringUtils.isBlank(serviceName)) {
            serviceName = rpcService.defaultValue();
        }else if(Arrays.stream(serviceNames).noneMatch(item -> item.equals(serviceName))) {
            throw new RpcException(RpcErrorEnum.SERVICE_NAME_NO_MATCH);
        }

        MethodPayload methodPayload = new MethodPayload();
        methodPayload.setMethod(method.getName());

        String requestId = UUID.randomUUID().toString();
        methodPayload.setRequestId(requestId);

        methodPayload.setArgsClass(method.getParameterTypes());
        methodPayload.setArgs(args);
        methodPayload.setServiceName(serviceName);

        ResultFuture requestFuture = new ResultFuture();
        requestFuture.setRequestId(requestId);
        FutureFactory.put(requestFuture);

        client.getSocketChannel().write(methodPayload);
        client.getSocketChannel().writeAndFlush(Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes()));

        ResultPayload resultPayload = FutureFactory.getData(requestId, ConfigCache.getRpcClientConfig().getTimeout());
        log.info("DefaultProxy 接口返回结果："+ resultPayload);

        return resultPayload != null ? resultPayload.getResult() : null;
    }

}
