package com.miao.webserver.core.connector.nio.cleaner;

import com.miao.webserver.core.connector.nio.poller.NioPoller;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Poller中  Map<SocketChannel, SocketChannelWrapper> sCToItWrapper;
 * 存储的数据需要定期清理，将关闭的超时的清理掉
 */
@Slf4j
public class SocketCleaner implements Runnable{

    private List<NioPoller> pollerList;
    private ScheduledExecutorService executor;

    public SocketCleaner(List<NioPoller> pollerList) {
        this.pollerList = pollerList;
    }

    public void start() {
        ThreadFactory factory = r -> new Thread(r, "SocketCleanerThread");
        executor = Executors.newSingleThreadScheduledExecutor(factory);
        executor.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }

    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public void run() {
        for (NioPoller poller : pollerList) {
            log.info("SocketCleaner开始检测Poller: {}", poller.getPollerName());
            poller.cleanTimeoutSocketChannel();
        }
        log.info("该Poller检测完毕");
    }
}
