package com.cy.rpc.client.proxy;

import com.cy.rpc.client.Client;
import com.cy.rpc.client.future.FutureFactory;
import com.cy.rpc.client.future.ResultFuture;
import com.cy.rpc.client.sockect.ClientFactory;
import com.cy.rpc.common.annotation.RpcService;
import com.cy.rpc.common.constant.MessageConstant;
import com.cy.rpc.common.exception.RpcException;
import com.cy.rpc.common.payload.MethodPayload;
import com.cy.rpc.common.payload.ResultPayload;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author chenyu3
 * 默认代理实现，向服务端发送消息
 */
@Getter
@Setter
@Slf4j
public class DefaultProxy implements InvocationHandler {

    private String appId;

    private String serviceName;

    public DefaultProxy(String localAddress, String serviceName) {
        this.appId = localAddress;
        this.serviceName = serviceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args){

        List<Client> clientList = ClientFactory.get(appId);
        if(clientList == null || clientList.size() == 0) {
            throw new RpcException(10001, "远程连接已关闭" + appId);
        }

        RpcService rpcService = method.getDeclaringClass().getAnnotation(RpcService.class);
        if(rpcService == null) {
            throw new RpcException(10001, "RpcService 为空");
        }

        String[] serviceNames = rpcService.value();

        if(StringUtils.isBlank(serviceName)) {
            serviceName = rpcService.defaultValue();
        }else if(Arrays.stream(serviceNames).noneMatch(item -> item.equals(serviceName))) {
            throw new RpcException(10001, "没有匹配的serviceName");
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

        //负载均衡到一条服务器，后期加策略
        int randomIndex = new Random().nextInt(clientList.size());
        clientList.get(randomIndex).getSocketChannel().write(methodPayload);
        clientList.get(randomIndex).getSocketChannel().writeAndFlush(Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes()));

        ResultPayload resultPayload = FutureFactory.getData(requestId);
        log.info("DefaultProxy 接口返回结果："+ resultPayload);

        return resultPayload != null ? resultPayload.getResult() : null;
    }

}
