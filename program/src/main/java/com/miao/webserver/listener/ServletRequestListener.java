package com.miao.webserver.listener;

import com.miao.webserver.listener.event.ServletRequestEvent;

import java.util.EventListener;

/**
 * 监听request的创建与销毁。用户每次请求request都会执行requestInitialized
 * request处理完毕自动销毁前执行requestDestroyed.
 * 注：若请求一个HTML页面包含多个图片，则可能触发多次request事件
 */
public interface ServletRequestListener extends EventListener{

    void requestInitialized(ServletRequestEvent requestEvent);

    void requestDestroyed(ServletRequestEvent requestEvent);
}
