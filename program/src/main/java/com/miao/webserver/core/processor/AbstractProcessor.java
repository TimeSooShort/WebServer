package com.miao.webserver.core.processor;

import com.miao.webserver.context.ServletContext;
import com.miao.webserver.context.WebApplication;
import com.miao.webserver.core.wrapper.SocketWrapper;
import com.miao.webserver.exception.handler.ExceptionHandler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 创建线程池，初始化ServletContext，初始化异常处理类ExceptionHandler
 */
public abstract class AbstractProcessor {

    protected ThreadPoolExecutor pool;
    protected ServletContext servletContext;
    protected ExceptionHandler exceptionHandler;

    public AbstractProcessor() {
        this.exceptionHandler = new ExceptionHandler();
        this.servletContext = WebApplication.getServletContext();
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
     * 关闭线程池,并调用servlet，filter，listener的destroy方法
     */
    public void shutdown() {
        pool.shutdown();
        servletContext.destroy();
    }

    /**
     * 处理请求，创建Request与Response
     * @param socketWrapper Socket包装类
     */
    public abstract void doProcess(SocketWrapper socketWrapper);
}
