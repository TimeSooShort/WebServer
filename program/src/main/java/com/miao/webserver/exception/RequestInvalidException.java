package com.miao.webserver.exception;

import com.miao.webserver.common.Constants;

/**
 * 请求数据不合规。
 * ExceptionHandler对该异常的处理是关闭socket抛弃该请求
 */
public class RequestInvalidException extends ServletException {

    private static final Constants.HttpStatus status = Constants.HttpStatus.BAD_REQUEST;

    public RequestInvalidException() {
        super(status);
    }
}
