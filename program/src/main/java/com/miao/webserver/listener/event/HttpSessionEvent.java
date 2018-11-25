package com.miao.webserver.listener.event;

import com.miao.webserver.Session.HttpSession;

import java.util.EventObject;

public class HttpSessionEvent extends EventObject{

    public HttpSessionEvent(HttpSession source) {
        super(source);
    }
}
