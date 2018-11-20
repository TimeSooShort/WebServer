package com.miao.webserver.listener;

import com.miao.webserver.listener.event.ServletContextEvent;

import java.util.EventListener;

/**
 * 监听context的创建与销毁，context代表当前的web应用程序。
 * 可用来启动时获取web.xml里面配置的参数信息
 */
public interface ServletContextListener extends EventListener {

    /**
     * 监听应用启动事件
     * @param contextEvent
     */
    void contextInitialized(ServletContextEvent contextEvent);

    /**
     * 监听应用关闭事件
     * @param contextEvent
     */
    void contextDestroyed(ServletContextEvent contextEvent);
}
