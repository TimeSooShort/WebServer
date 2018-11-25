package com.miao.webserver.core.connector.nio;

import com.miao.webserver.core.endpoint.nio.NioEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
public class NioConnector implements Runnable {

    private NioEndpoint endpoint;

    public NioConnector(NioEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void run() {
        while (endpoint.isRunning()) {
            SocketChannel client = null;
            try {
                // serverSocketChannel.accept()，阻塞模式
                client = endpoint.getSocketChannel();
                client.configureBlocking(false);
                log.info("Acceptor接收到的请求:{}", client);
                endpoint.chooseOnePoller(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
