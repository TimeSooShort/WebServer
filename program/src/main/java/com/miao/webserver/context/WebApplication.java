package com.miao.webserver.context;

import org.dom4j.DocumentException;

/**
 * 创建ServletContext的静态对象，在项目启动时初始化，运行期间只有一个该对象
 */
public class WebApplication {
    private static ServletContext servletContext;

    static {
        try {
            servletContext = new ServletContext();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }
}
