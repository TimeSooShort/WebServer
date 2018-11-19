package com.miao.webserver.filter;

import com.miao.webserver.exception.ServletException;
import com.miao.webserver.request.Request;
import com.miao.webserver.response.Response;

import java.io.IOException;

/**
 * 一个资源可以跟多个过滤器关联，FilterChain.doFilter通常引发调用链中的
 * 下一个过滤器被调用。我们称对某些web资源进行拦截的一组过滤器为过滤器链
 * 执行顺序与<filter-mapping>有关，谁在前先执行谁
 */
public interface FilterChain {

    void doFilter(Request request, Response response);
}
