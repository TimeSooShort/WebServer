package com.miao.webserver.context.holder;

import com.miao.webserver.servlet.Servlet;
import lombok.Getter;
import lombok.Setter;

/**
 * servletUrl代表servlet实例的路径
 */
@Getter
@Setter
public class ServletHolder {
    private Servlet servlet;
    private String servletUrl;

    public ServletHolder(String servletUrl) {
        this.servletUrl = servletUrl;
    }
}
