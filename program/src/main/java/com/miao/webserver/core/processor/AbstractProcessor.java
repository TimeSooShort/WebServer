package com.miao.webserver.core.processor;

import com.miao.webserver.core.wrapper.SocketWrapper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractProcessor {

    protected ThreadPoolExecutor pool;

    public AbstractProcessor() {

        ThreadFactory threadFactory = new ThreadFactory() {
            private int count;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Work Thread " + count++);
            }
        };
        pool = new ThreadPoolExecutor(100, 150, 1,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        pool.shutdown();
    }

    /**
     * 处理请求，创建Request与Response
     * @param socketWrapper Socket包装类
     */
    public abstract void doProcess(SocketWrapper socketWrapper);
}
