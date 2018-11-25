package com.miao.webserver.listener;

import com.miao.webserver.Session.HttpSession;
import com.miao.webserver.listener.event.HttpSessionEvent;

import java.util.EventListener;

public interface HttpSessionListener extends EventListener{

    /**
     * session创建
     * @param event
     */
    void sessionCreated(HttpSessionEvent event);

    /**
     * session销毁
     * @param event
     */
    void sessionDestroyed(HttpSessionEvent event);
}
