package com.miao.webserver.exception;

import com.miao.webserver.common.Constants.HttpStatus;

public class ServletNotFoundException extends ServletException {
    private static final HttpStatus status = HttpStatus.NOT_FOUNT;
    public ServletNotFoundException() {
        super(status);
    }
}
