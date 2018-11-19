package com.miao.webserver.core.handler;

import com.miao.webserver.context.ServletContext;
import com.miao.webserver.core.wrapper.SocketWrapper;
import com.miao.webserver.exception.ServerErrorException;
import com.miao.webserver.exception.ServletException;
import com.miao.webserver.exception.ServletNotFoundException;
import com.miao.webserver.exception.handler.ExceptionHandler;
import com.miao.webserver.filter.Filter;
import com.miao.webserver.filter.FilterChain;
import com.miao.webserver.request.Request;
import com.miao.webserver.response.Response;
import com.miao.webserver.servlet.Servlet;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Connector监听请求，Processor解析请求创建Request，Response，
 * 之后需要做的就是根据url来获取静态资源或是servlet调用其service方法
 *
 * 从Request中获取路径url，从ServletContext中的servlet map中获取servlet实例
 */
@Slf4j
public abstract class AbstractHandler implements Runnable, FilterChain{

    protected Request request;
    protected Response response;
    protected SocketWrapper socketWrapper;
    protected ServletContext servletContext;
    protected Servlet servlet;
    protected List<Filter> filters;
    protected ExceptionHandler exceptionHandler;
    private int filterIndex; // 每个请求都有自己的Handler实例，所以是线程安全的

    public AbstractHandler(Request request, Response response,
                           SocketWrapper socketWrapper, ServletContext servletContext,
                           ExceptionHandler exceptionHandler)
            throws ServletException {
        this.request = request;
        this.response = response;
        this.socketWrapper = socketWrapper;
        this.servletContext = servletContext;
        this.exceptionHandler = exceptionHandler;
        // 通过Request里被分析得出的url来获取当前请求的servlet实例
        servlet = servletContext.mapServlet(request.getUrl());
        // 通过url获取Filter集合，可能为空
        filters = servletContext.mapFilter(request.getUrl());
    }

    @Override
    public void run() {
        if (filters.isEmpty()) {
            service();
        } else {
            doFilter(request, response);
        }
    }

    private void service(){
        try {
            // 这里是单例的，多线程返回一个实例，是共享的
            // 所以客户端程序员的servlet类需要是线程安全的

            // 出于安全的考虑传给用户端的是request与response的外观类FacadeRequest，FacadeResponse
            // 因为不希望将request与response开放给用户。
            servlet.service(request, response);
        }
        catch (ServletException e) {
            exceptionHandler.handle(e, response, socketWrapper);
        }
        // Servlet程序员可能会在他的service方法抛一些业务异常等
        // 这里捕获这些异常统一定位成500错误,跳到errors/500.html
        // 所以不建议将业务代码的异常再service()中往上抛，应该自己捕获异常并处理
        // 否则，到这里就统一跳到500
        catch (Exception e) {
            e.printStackTrace();
            exceptionHandler.handle(new ServerErrorException(), response, socketWrapper);
        } finally {
            flushResponse();
        }
        log.info("请求处理完毕");
    }

    /**
     * 关于过滤链如何不断往下执行问题的实现：
     * 客户端程序员在自己实现的xxxFilter类的doFilter方法中来决定是否放行
     * 此处放行指的是，在doFilter中调用FilterChain的doFilter方法，而客户端的
     * FilterChain就是AbstractHandler，也就是如果放行就再次调用本方法
     * 所以在这个方法中的实现就是循环Filter集合调用其doFilter方法
     * @param request
     * @param response
     */
    @Override
    public void doFilter(Request request, Response response) {
        if (filterIndex < this.filters.size()) {
            try {
                this.filters.get(filterIndex++).doFilter(request, response, this);
            } catch (ServletException e) {
                exceptionHandler.handle(e, response, socketWrapper);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            service(); // Filter执行完后调用service()
        }
    }

    /**
     * 响应数据写回到输出流中，由bio, nio各个模式自己去实现
     */
    public abstract void flushResponse();
}
