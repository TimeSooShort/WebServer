package com.miao.webserver.filter;

import com.miao.webserver.exception.ServletException;
import com.miao.webserver.request.Request;
import com.miao.webserver.response.Response;

import java.io.IOException;

public interface Filter {

    /**
     * 容器启动时调用，用于初始化相关操作
     * servlet规范里的Filter的init有一个FilterConfig参数
     * 该对象拥有如getFilterName, getServletContext, getInitParameter等方法
     * @throws ServletException
     */
    void init() throws ServletException;

    /**
     * 过滤操作
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    void doFilter(Request request, Response response, FilterChain filterChain) throws ServletException, IOException;

    /**
     * 过滤器终止前调用
     */
    void destroy();
}
