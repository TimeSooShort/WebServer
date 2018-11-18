package com.miao.webserver.exception;

import com.miao.webserver.common.Constants.HttpStatus;
import lombok.Getter;

/**
 * 根异常
 */
@Getter
public class ServletException extends Exception {
    private HttpStatus status;
    public ServletException(HttpStatus status) {
        super(status.name() +": " + status.getCode());
        this.status = status;
    }
}
