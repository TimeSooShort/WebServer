package com.miao.webserver.core.endpoint.bio;

import com.miao.webserver.core.connector.bio.BioConnector;
import com.miao.webserver.core.endpoint.Endpoint;
import com.miao.webserver.core.processor.AbstractProcessor;
import com.miao.webserver.core.processor.bio.BioProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class BioEndpoint extends Endpoint{

    private AbstractProcessor processor;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = true;

    /**
     * 创建BioProcessor, ServerSocket, BioConnector, 开启一个新线程
     * @param port 端口
     */
    @Override
    public void start(int port) {
        try {
            processor = new BioProcessor();
            serverSocket = new ServerSocket(port);
            startThread();
            log.info("服务器启动");
        } catch (IOException e) {
            e.printStackTrace();
            log.info("初始化服务器失败");
            close();
        }
    }

    /**
     * 开启一个新线程轮循等待客户端请求，BioConnector实现了Runnable
     */
    private void startThread() {
        BioConnector connector = new BioConnector(this, processor);
        Thread thread = new Thread(connector, "bio-acceptor");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 关闭服务器：
     */
    @Override
    public void close() {
        isRunning = false;
        processor.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用serverSocket的accept（)方法
     * @return 请求socket
     * @throws IOException
     */
    public Socket accept() throws IOException {
        return serverSocket.accept();
    }

    /**
     * 服务器是否正在运行
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }
}
