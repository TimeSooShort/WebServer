package com.miao.webserver.core.processor.bio;

import com.miao.webserver.core.handler.bio.BioHandler;
import com.miao.webserver.core.processor.AbstractProcessor;
import com.miao.webserver.core.wrapper.SocketWrapper;
import com.miao.webserver.core.wrapper.bio.BioSocketWrapper;
import com.miao.webserver.exception.RequestInvalidException;
import com.miao.webserver.exception.ServletException;
import com.miao.webserver.request.Request;
import com.miao.webserver.response.Response;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 处理请求，生成Request与Response对象
 */
public class BioProcessor extends AbstractProcessor {

    @Override
    public void doProcess(SocketWrapper socketWrapper) {
        Socket socket = ((BioSocketWrapper) socketWrapper).getSocket();
        Request request;
        Response response = null;
        try {
            // socket会在一切处理完后被关闭，其input与output都将因socket的关闭而关闭
            // socket的关闭在AbstractHandler的flushResponse中，该方法负责在service()执行完成后将数据
            BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
            byte[] buffer = new byte[input.available()];
            int len = input.read(buffer);
            if (len <= 0) {
                throw new RequestInvalidException(); // exceptionHandler对该异常的处理是关闭socket抛弃该请求
            }
            request = new Request(buffer);
            response = new Response();
            pool.execute(new BioHandler(request, response, socketWrapper,
                    servletContext, exceptionHandler));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, socketWrapper);
        }
    }
}
