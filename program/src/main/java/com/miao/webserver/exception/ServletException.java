package com.miao.webserver.exception;

import com.miao.webserver.common.Constants;
import lombok.Getter;

/**
 * 根异常
 */
public class ServletException extends Exception {
    public ServletException(Constants.HttpStatus status) {
        super(status.name() +": " + status.getCode());
    }
}
