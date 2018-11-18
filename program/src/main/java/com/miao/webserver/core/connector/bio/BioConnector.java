package com.miao.webserver.core.connector.bio;

import com.miao.webserver.core.endpoint.Endpoint;
import com.miao.webserver.core.endpoint.bio.BioEndpoint;
import com.miao.webserver.core.processor.AbstractProcessor;
import com.miao.webserver.core.wrapper.bio.BioSocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

/**
 * BioConnector实现了Runnable，监听客户端请求，将socket传给processor
 */
@Slf4j
public class BioConnector implements Runnable {

    private BioEndpoint server;
    private AbstractProcessor bioProcessor;

    public BioConnector(Endpoint bioEndpoint, AbstractProcessor bioProcessor) {
        this.server = (BioEndpoint) bioEndpoint;
        this.bioProcessor = bioProcessor;
    }

    @Override
    public void run() {
        log.info("监听开始");
        while (server.isRunning()) {
            Socket socket = null;
            try {
                socket = server.accept();
            } catch (IOException e) {
                // 出现异常就抛弃本次连接，重新等待下次，
                // 由于会在循环判断服务器是否被关闭，所以不用担心死循环问题
                continue;
            }
            log.info("client:{}", socket);
            bioProcessor.doProcess(new BioSocketWrapper(socket));
        }

    }
}
