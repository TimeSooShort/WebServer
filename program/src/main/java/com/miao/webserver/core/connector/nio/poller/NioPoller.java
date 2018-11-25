package com.miao.webserver.core.connector.nio.poller;

import com.miao.webserver.core.endpoint.nio.NioEndpoint;
import com.miao.webserver.core.wrapper.SocketWrapper;
import com.miao.webserver.core.wrapper.nio.SocketChannelWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.Wrapper;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
public class NioPoller implements Runnable{

    private Selector selector;
    private NioEndpoint endpoint;
    private Queue<PollerEvent> eventQueue;
    private Map<SocketChannel, SocketChannelWrapper> sCToItWrapper;
    private String pollerName;

//    // 并发情况下将wrapper作为成员变量这种设计会出问题，
//    // socketChannel与socketChannelWrapper不匹配问题
//    private SocketChannelWrapper wrapper;

    public NioPoller(NioEndpoint endpoint, String name) throws IOException {
        selector = Selector.open();
        this.endpoint = endpoint;
        this.eventQueue = new ConcurrentLinkedDeque<>();
        this.sCToItWrapper = new ConcurrentHashMap<>();
        this.pollerName = name;
    }

    @Override
    public void run() {
        log.info("开始监听：{}", Thread.currentThread().getName());
        while (endpoint.isRunning()) {
            try {
                registerQueuesEvents();
                if (selector.select() == 0) {
                    continue;// 唤醒后让其去注册队列中的SocketChannel
                }
                log.info("select返回，遍历全部监听channel的情况");
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    // 读就绪
                    if (selectionKey.isReadable()) {
                        log.info("读就绪");
                        // 在将socketChannel注册到selector时，register方法的
                        // 第三个参数传的是由socketChannel封装成的socketChannelWrapper
                        // 使用该种方式来确保传给processor的socketWrapper是本次处理的socketChannel的wrapper
                        SocketChannelWrapper attachment = (SocketChannelWrapper) selectionKey.attachment();
                        if (attachment != null) {
                            // 处理这个读就绪的socketChannel
                            attachment.setWorking(true);
                            endpoint.execute(attachment);
                        }
                    }
                    // Selector不会自己从已选择键集中移除SelectionKey实例。
                    // 必须在处理完通道时自己移除。下次该通道变成就绪时，
                    // Selector会再次将其放入已选择键集中。
                    iterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * isNew：true 代表是新的请求，封装成wrapper，进一步构建PollerEvent，
     * 最后加入队列
     * isNew：false 代表该连接没有关闭，将其构造的PollerEvent放入队列
     * @param socketChannel
     * @param isNew
     */
    public void wrapItAndAddToQueue(SocketChannel socketChannel, boolean isNew) {
        log.info("Acceptor将接收到的socketChannel放到 {} 的Queue中", pollerName);
        SocketChannelWrapper wrapper = null;
        if (isNew) {
            wrapper = new SocketChannelWrapper(socketChannel, this.selector, this);
            sCToItWrapper.put(socketChannel, wrapper);
        } else {
            wrapper = sCToItWrapper.get(socketChannel);
            wrapper.setWorking(false);
        }
        wrapper.setWaitBegin(System.currentTimeMillis());
        // 创建该请求的PollerEvent对象
        PollerEvent event = new PollerEvent(wrapper);
        // 加进队列
        eventQueue.offer(event);
        // 由于select方法阻塞，这里在向poller队列添加任务后
        // 唤醒线程调用队列中PollerEvent的registerSocketChannel注册
        selector.wakeup();
    }

    /**
     * 注册队列中的PollerEvent到该Poller的Selector
     */
    private void registerQueuesEvents() {
        log.info("Queue的大小为:{}，清空Queue， 将socketChannel注册到selector中");
        PollerEvent event = null;
        // 并发下这里采用两重保险：此时队列里event个数与返回不为null
        // 没有个数限制只以返回不为null来判断的话，则Poller线程可能长时间困在注册任务这一步
        for (int i = 0; i < eventQueue.size() &&
                (event = eventQueue.poll()) != null; i++) {
            event.registerSocketChannel();
        }
    }

    public Selector getSelector() {
        return selector;
    }

    public String getPollerName() {
        return pollerName;
    }

    /**
     * 关闭selector，socketChannel，queue与map
     * @throws IOException
     */
    public void close() throws IOException {
        for (SocketChannelWrapper wrapper : sCToItWrapper.values()) {
            wrapper.close();
        }
        eventQueue.clear();
        selector.close();
        sCToItWrapper.clear();
    }

    /**
     * 在socketChannelWrapper中的waitBegin字段记录当初被放入poller队列的时间
     * 专门的清理线程定期检查map中时间超过一分钟的socket
     */
    public void cleanTimeoutSocketChannel() {
        Iterator<Map.Entry<SocketChannel, SocketChannelWrapper>> iterator =
                this.sCToItWrapper.entrySet().iterator();
        while (iterator.hasNext()) {
            SocketChannelWrapper wrapper = iterator.next().getValue();
            log.info("开始清理Poller：{} Map中超时socketChannel，检查对象；{}",
                    this.pollerName, wrapper);
            if (!wrapper.getSocketChannel().isConnected()) {
                log.info("该socketChannel已被关闭");
                iterator.remove();
                continue;
            }
            if (wrapper.isWorking()) {
                log.info("该socketChannel正在工作中, 不应关闭");
                continue;
            }
            if (System.currentTimeMillis() - wrapper.getWaitBegin() >
                    NioEndpoint.KEEP_ALIVE_TIME) {
                log.info("{} keepAlive已过期", wrapper.getSocketChannel());
                try {
                    wrapper.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                iterator.remove();
            }
        }
    }
}
