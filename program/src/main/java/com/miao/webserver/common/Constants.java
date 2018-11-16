package com.miao.webserver.common;

import java.nio.charset.Charset;

public class Constants {

    public static final String UTF_8 = "UTF-8";

    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    public static final String CRLF = "\r\n";

    public static final String BLANK = " ";

    public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=utf-8";

    public enum HttpStatus {
        OK(200), NOT_FOUNT(404), INTERNAL_SERVER_ERROR(500),
        BAD_REQUEST(400), MOVED_TEMPORARILY(302);

        private int code;

        HttpStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
