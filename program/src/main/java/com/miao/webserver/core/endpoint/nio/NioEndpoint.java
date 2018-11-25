package com.miao.webserver.core.endpoint.nio;

import com.miao.webserver.core.connector.nio.NioConnector;
import com.miao.webserver.core.connector.nio.cleaner.SocketCleaner;
import com.miao.webserver.core.connector.nio.poller.NioPoller;
import com.miao.webserver.core.endpoint.Endpoint;
import com.miao.webserver.core.processor.nio.NioProcessor;
import com.miao.webserver.core.wrapper.nio.SocketChannelWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NioEndpoint extends Endpoint {

    private NioProcessor processor;
    private NioConnector connector;
    private ServerSocketChannel serverSocketChannel;
    private List<NioPoller> nioPollerList;
    private int pollerCount = Math.min(2, Runtime.getRuntime().availableProcessors());
    private AtomicInteger pollerRotate = new AtomicInteger(0);

    private volatile boolean isRunning = true;

    public final static int KEEP_ALIVE_TIME = 60 * 1000;  // 1min

    private SocketCleaner cleaner;

    @Override
    public void start(int port) {
        try {
            initNioProcessor();
            initPoller();
            initNioConnector();
            initServerSocket(port);
            initSocketCleaner(this.nioPollerList);
            log.info("服务器启动");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("服务器初始化失败");
            close();
        }
    }

    /**
     * 初始化SocketCleaner：定期清理线程
     * @param nioPollerList
     */
    private void initSocketCleaner(List<NioPoller> nioPollerList) {
        cleaner = new SocketCleaner(nioPollerList);
        cleaner.start();
    }


    /**
     * 每一个poller对应一个selector
     */
    private void initPoller() throws IOException {
        this.nioPollerList = new ArrayList<>();
        for (int i = 0; i < pollerCount; i++) {
            String pollerName = "Poller-Thread-" + i;
            NioPoller poller = new NioPoller(this, pollerName);
            this.nioPollerList.add(poller);
            Thread thread = new Thread(poller, pollerName);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * 轮循选择一个Poller
     * @return
     */
    private NioPoller getPoller() {
        // 超过最大值后变为负数
        int index = Math.abs(this.pollerRotate.getAndIncrement()) % this.pollerCount;
        return this.nioPollerList.get(index);
    }

    /**
     * 创建ServerSocketChannel
     * @param port 监听的端口
     * @throws IOException
     */
    private void initServerSocket(int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
    }

    /**
     * connector是Runnable用来监听请求
     */
    private void initNioConnector() {
        this.connector = new NioConnector(this);
        Thread thread = new Thread(connector, "Connector-Thread");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 初始化processor，AbstractProcessor类中初始化了servletContext，线程池
     */
    private void initNioProcessor() {
        this.processor = new NioProcessor();
    }

    /**
     * 关闭服务器
     */
    @Override
    public void close() {
        this.isRunning = false;
        cleaner.shutdown(); // 关闭清理线程
        for (NioPoller poller : this.nioPollerList) {
            try {
                poller.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        processor.shutdown(); // 关闭线程池，触发ServletContext关闭事件
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SocketChannel getSocketChannel() throws IOException {
        return this.serverSocketChannel.accept();
    }

    /**
     * 将SocketChannel封装成SocketChannelWrapper， 再进一步成PollerEvent，
     * 代表该Poller要处理的事件，加入Poller的队列中，Poller会不断循环该队列取出任务执行
     * @param socketChannel
     */
    public void chooseOnePoller(SocketChannel socketChannel) {
        getPoller().wrapItAndAddToQueue(socketChannel, true);
    }

    /**
     * 服务器是否被关闭
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * 调用NioProcessor的doProcess来处理这里读已就绪的客户端连接
     * @param wrapper
     */
    public void execute(SocketChannelWrapper wrapper) {
        processor.doProcess(wrapper);
    }
}
