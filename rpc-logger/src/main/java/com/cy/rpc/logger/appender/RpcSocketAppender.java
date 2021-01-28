package com.cy.rpc.logger.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.cy.rpc.logger.LoggerSocket;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenyu3
 * 监控socket日志appender
 */
@Getter
@Setter
public class RpcSocketAppender extends AppenderBase<ILoggingEvent> {

    private LoggerSocket socket;

    private String appName;

    private String remoteAddress;

    private String port;

    public void start() {
        socket = new LoggerSocket("localhost", 1111);
        try {
            if(socket.connect()) {
                this.addInfo("");
            }
        }catch (Exception e){

        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        socket.getSocketChannel().write(iLoggingEvent);
        socket.getSocketChannel().writeAndFlush("$FINISH$");
    }
}
