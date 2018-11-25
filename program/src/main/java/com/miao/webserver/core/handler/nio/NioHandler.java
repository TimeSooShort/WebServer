package com.miao.webserver.core.handler.nio;

import com.miao.webserver.context.ServletContext;
import com.miao.webserver.core.handler.AbstractHandler;
import com.miao.webserver.core.wrapper.SocketWrapper;
import com.miao.webserver.core.wrapper.nio.SocketChannelWrapper;
import com.miao.webserver.exception.ServletException;
import com.miao.webserver.exception.handler.ExceptionHandler;
import com.miao.webserver.request.Request;
import com.miao.webserver.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

@Slf4j
public class NioHandler extends AbstractHandler{

    public NioHandler(Request request, Response response, SocketWrapper socketWrapper,
                      ServletContext servletContext, ExceptionHandler exceptionHandler)
            throws ServletException {
        super(request, response, socketWrapper, servletContext, exceptionHandler);
    }

    /**
     * 写入后根据请求头的Connection来判断是关闭连接还是重新放回Poller
     */
    @Override
    public void flushResponse() {
        SocketChannelWrapper wrapper = (SocketChannelWrapper) socketWrapper;
        ByteBuffer responseData = response.getResponseByteBuffer();
        try {
            SocketChannel socketChannel = wrapper.getSocketChannel();
            while (responseData.hasRemaining()) {
                socketChannel.write(responseData);
            }
            List<String> connection = request.getHeaders().get("Connection");
            if (connection != null && connection.get(0).equals("close")) {
                log.info("CLOSE:客户端连接:{} 已关闭", socketChannel);
                wrapper.close();
            } else {
                log.info("KEEP-ALIVE: 客户端连接重新注册到Poller中");
                wrapper.getPoller().wrapItAndAddToQueue(socketChannel, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        servletContext.afterRequestDestroyed(request);
    }
}
