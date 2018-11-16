package com.miao.webserver.exception;

import com.miao.webserver.common.Constants;

/**
 * 请求数据不合法
 */
public class RequestInvalidException extends ServletException {

    private static final Constants.HttpStatus status = Constants.HttpStatus.BAD_REQUEST;

    public RequestInvalidException() {
        super(status);
    }
}
