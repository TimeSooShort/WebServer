package com.miao.webserver.core.processor.bio;

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
        Request request = null;
        Response response = null;
        try {
            BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
            byte[] buffer = new byte[input.available()];
            int len = input.read(buffer);
            if (len <= 0) {
                throw new RequestInvalidException();
            }
            request = new Request(buffer);
            response = new Response();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            // exceptionHandler.handle
        }
    }
}
