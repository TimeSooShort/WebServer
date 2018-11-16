package com.miao.webserver.exception;

import com.miao.webserver.common.Constants;

/**
 * 解析请求异常
 */
public class RequestParseException extends ServletException {
    private static final Constants.HttpStatus STATUS = Constants.HttpStatus.BAD_REQUEST;
    public RequestParseException() {
        super(STATUS);
    }
}
