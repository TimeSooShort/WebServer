package com.miao.webserver.listener.event;

import com.miao.webserver.context.ServletContext;

import java.util.EventObject;

/**
 * ServletContext相关的事件
 */
public class ServletContextEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ServletContextEvent(ServletContext source) {
        super(source);
    }

    public ServletContext getServletContext() {
        return (ServletContext) super.getSource();
    }
}
