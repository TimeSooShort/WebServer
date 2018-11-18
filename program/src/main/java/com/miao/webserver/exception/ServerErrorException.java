package com.miao.webserver.exception;

import  com.miao.webserver.common.Constants.HttpStatus;

/**
 * 服务器异常，500
 */
public class ServerErrorException extends ServletException {
    private static final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    public ServerErrorException() {
        super(status);
    }
}
