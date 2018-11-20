package com.miao.webserver.listener.event;

import com.miao.webserver.context.ServletContext;
import com.miao.webserver.request.Request;

import java.util.EventObject;

/**
 * request相关事件
 */
public class ServletRequestEvent extends EventObject {

    public ServletRequestEvent(Request source) {
        super(source);
    }

    public ServletContext getServletContext() {
        return (ServletContext) super.getSource();
    }
}
