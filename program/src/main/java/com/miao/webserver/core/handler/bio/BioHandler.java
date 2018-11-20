package com.miao.webserver.core.handler.bio;

import com.miao.webserver.context.ServletContext;
import com.miao.webserver.context.WebApplication;
import com.miao.webserver.core.handler.AbstractHandler;
import com.miao.webserver.core.wrapper.SocketWrapper;
import com.miao.webserver.core.wrapper.bio.BioSocketWrapper;
import com.miao.webserver.exception.ServletException;
import com.miao.webserver.exception.ServletNotFoundException;
import com.miao.webserver.exception.handler.ExceptionHandler;
import com.miao.webserver.request.Request;
import com.miao.webserver.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 继承自AbstractHandler，实现自己的flushResponse行为
 */
@Slf4j
public class BioHandler extends AbstractHandler {

    public BioHandler(Request request, Response response, SocketWrapper socketWrapper,
                      ServletContext servletContext, ExceptionHandler exceptionHandler)
            throws ServletException {
        super(request, response, socketWrapper, servletContext, exceptionHandler);
    }

    /**
     * 数据写入到输出流
     */
    @Override
    public void flushResponse() {
        BioSocketWrapper wrapper = (BioSocketWrapper) socketWrapper;
        // 获取已传入Response的数据body[],那么数据什么时候传到body里的？
        // 在sample包中的例子里：在Servlet的service()里指定访问的页面，
        // 调用request的方法解析它，并将数据传入到response的body字段中。
        // 这里只允许通过这种方式来使用。
        byte[] bytes = response.getResponseBytes();
        OutputStream output = null;
        try {
            output = wrapper.getSocket().getOutputStream();
            output.write(bytes);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("数据写入输出流发生错误");
        } finally {
            try {
                if (output != null) output.close();
                wrapper.close(); // 关闭socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // request销毁后触发监听事件
        servletContext.afterRequestDestroyed(request);
    }
}
