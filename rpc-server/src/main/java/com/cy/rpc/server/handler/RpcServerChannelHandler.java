package com.cy.rpc.server.handler;

import com.cy.rpc.common.constant.MessageConstant;
import com.cy.rpc.common.exception.RpcException;
import com.cy.rpc.common.payload.MethodPayload;
import com.cy.rpc.common.payload.ResultPayload;
import com.cy.rpc.server.service.AbstractServiceFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author chenyu3
 * rpc远程接口调用
 */
@Slf4j
public class RpcServerChannelHandler extends SimpleChannelInboundHandler<MethodPayload> {

    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("rpc-server-handler-%d").build();
    private static final ExecutorService executor = new ThreadPoolExecutor(
            10, 200, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(60), threadFactory);

    private ChannelHandlerContext channelHandlerContext;

    //获取Service抽象工厂类
    private final AbstractServiceFactory abstractServiceFactory;

    public RpcServerChannelHandler(AbstractServiceFactory abstractServiceFactory){
        this.abstractServiceFactory = abstractServiceFactory;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("rpc server channel active");
        this.channelHandlerContext = ctx;
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("rpc server channel inactive");
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, MethodPayload msg) {
        //一个channel对应一个eventLoop线程，IO单线程，复杂业务逻辑采用多线程
        ctx.channel().eventLoop().execute(() -> {
            executeMethod(msg);
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        log.info("rpc server channel read complete");
        ctx.fireChannelReadComplete();
    }

    /**
     * 异步执行方法调用
     * @param methodPayload
     */
    private void executeMethod(MethodPayload methodPayload) {
        executor.execute(() -> {
            //返回结果
            ResultPayload resultPayload = new ResultPayload();
            resultPayload.setRequestId(methodPayload.getRequestId());

            String serviceName = methodPayload.getServiceName();
            Object service = abstractServiceFactory.getServiceByName(serviceName);
            if(service == null) {
                resultPayload.setCode(10003);
                resultPayload.setMessage("服务未找到！");
                channelHandlerContext.channel().write(resultPayload);
                channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes()));
                return;
            }

            //方法返回具体结果
            try {
                Object[] args = methodPayload.getArgs();
                Object returnValue = service.getClass().getMethod(methodPayload.getMethod(), methodPayload.getArgsClass()).invoke(service, args);

                resultPayload.setResult(returnValue);
                resultPayload.setCode(1);
                resultPayload.setMessage("success");
                resultPayload.setSuccess(true);

                channelHandlerContext.channel().write(resultPayload);
                channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes()));
            }catch (RpcException e){
                resultPayload.setCode(10003);
                resultPayload.setMessage(e.getMessage());
                channelHandlerContext.channel().write(resultPayload);
                channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes()));
            } catch (Exception e){
                resultPayload.setCode(10005);
                resultPayload.setMessage("接口调用异常！");
                channelHandlerContext.channel().write(null);
                channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer(MessageConstant.FINISH.getBytes()));
            }

        });
    }

}
