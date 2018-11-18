package com.miao.webserver.exception.handler;

import com.miao.webserver.core.wrapper.SocketWrapper;
import com.miao.webserver.exception.RequestInvalidException;
import com.miao.webserver.exception.ServletException;
import com.miao.webserver.response.Header;
import com.miao.webserver.response.Response;
import com.miao.webserver.util.IOUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.miao.webserver.common.Constants.ContextConstant.ERROR_PAGE;

/**
 * 异常处理器。根据不同异常设置响应Response信息
 */
@Slf4j
public class ExceptionHandler {

    public void handle(ServletException e, Response response, SocketWrapper wrapper) {
        try {
            if (e instanceof RequestInvalidException) {
                log.info("请求无法读取，丢弃");
                wrapper.close();
            } else {
                log.info("抛出异常:{}", e.getClass().getName());
                e.printStackTrace();
                // 断开持久连接：Connection: close
                response.addHeader(new Header("Connection", "close"));
                response.setStatus(e.getStatus());
                response.setBody(IOUtil.getBytesFromFile(
                        String.format(ERROR_PAGE, String.valueOf(e.getStatus().getCode()))
                ));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
