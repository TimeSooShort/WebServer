package com.miao.webserver.exception;

import com.miao.webserver.common.Constants.HttpStatus;

/**
 * Filter失踪异常
 */
public class FilterNotFoundException extends ServletException {
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUNT;
    public FilterNotFoundException() {
        super(STATUS);
    }
}
