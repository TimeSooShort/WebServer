package com.miao.webserver.core.processor.nio;

import com.miao.webserver.core.handler.nio.NioHandler;
import com.miao.webserver.core.processor.AbstractProcessor;
import com.miao.webserver.core.wrapper.SocketWrapper;
import com.miao.webserver.core.wrapper.nio.SocketChannelWrapper;
import com.miao.webserver.exception.RequestInvalidException;
import com.miao.webserver.exception.RequestParseException;
import com.miao.webserver.exception.ServerErrorException;
import com.miao.webserver.exception.ServletException;
import com.miao.webserver.request.Request;
import com.miao.webserver.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class NioProcessor extends AbstractProcessor {

    /**
     * 读取request，构建Request， Response， 构建NioHandler放入线程池中处理
     * @param socketWrapper Socket包装类
     */
    @Override
    public void doProcess(SocketWrapper socketWrapper) {
        SocketChannelWrapper wrapper = (SocketChannelWrapper) socketWrapper;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Request request = null;
        Response response = new Response();
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            log.info("开始读取Request");
            while (wrapper.getSocketChannel().read(buffer) > 0) {
                buffer.flip();
                output.write(buffer.array());
            }
            output.close();
            request = new Request(output.toByteArray());
            request.setResponse(response);
            pool.execute(new NioHandler(request, response, wrapper, servletContext, exceptionHandler));
        } catch (IOException e) {
            e.printStackTrace();
            exceptionHandler.handle(new ServerErrorException(), response, wrapper);
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, wrapper);
        }
    }
}
