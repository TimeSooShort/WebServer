package com.miao.webserver.Session;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 过期session定期清理线程
 */
@Slf4j
public class IdleSessionCleaner implements Runnable {

    private ScheduledExecutorService executor;

    public IdleSessionCleaner() {
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "IdleSessionCleaner"));
    }

    public void start() {
        executor.scheduleAtFixedRate(this, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        log.info("开始扫描过期session...");
        SessionManager.getManager().cleanIdleSession();
        log.info("扫描结束...");
    }
}
