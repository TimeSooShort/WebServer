package com.miao.webserver.response;

import com.miao.webserver.common.Constants.HttpStatus;
import com.miao.webserver.cookie.Cookie;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.miao.webserver.common.Constants.DEFAULT_CONTENT_TYPE;
import static com.miao.webserver.common.Constants.BLANK;
import static com.miao.webserver.common.Constants.CRLF;
import static com.miao.webserver.common.Constants.UTF_8_CHARSET;

@Slf4j
public class Response {

    private StringBuilder headerBuilder;
    private List<Cookie> cookies;
    private List<Header> headers;
    private HttpStatus status = HttpStatus.OK;
    private String contentType = DEFAULT_CONTENT_TYPE;
    private byte[] body = new byte[0];

    public Response() {
        this.headerBuilder = new StringBuilder();
        this.cookies = new ArrayList<>();
        this.headers = new ArrayList<>();
    }

    /**
     * 设置状态
     * @param status
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    /**
     * 设置实体对象的类型
     * @param contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 传入返回内容
     * @param body
     */
    public void setBody(byte[] body) {
        this.body = body;
    }

    /**
     * 添加cookie
     * @param cookie
     */
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    /**
     * 添加响应头
     * @param header
     */
    public void addHeader(Header header) {
        this.headers.add(header);
    }

    /**
     * 构建响应头
     */
    private void buildHeader() {
        // HTTP/1.1 200 OK
        headerBuilder.append("HTTP/1.1").append(BLANK).append(status.getCode())
                .append(BLANK).append(status.name()).append(CRLF);
        // Date: Fri Nov 16 20:07:58 CST 2018
        headerBuilder.append("Date:").append(BLANK).append(new Date()).append(CRLF);
        headerBuilder.append("Content-Type:").append(BLANK).append(contentType).append(CRLF);
        if (!headers.isEmpty()) {
            for (Header header : headers) {
                headerBuilder.append(header.getKey()).append(":").append(BLANK)
                        .append(header.getValue()).append(CRLF);
            }
        }
        if (!cookies.isEmpty()) {
            for (Cookie cookie : cookies) {
                headerBuilder.append("Set-Cookie:").append(BLANK).append(cookie.getKey())
                        .append("=").append(cookie.getValue()).append(CRLF);
            }
        }
        headerBuilder.append("Content-Length:").append(BLANK).append(body.length).append(CRLF).append(CRLF);
    }

    /**
     * 返回完整Response的byte[]
     * @return
     */
    public byte[] getResponseBytes() {
        buildHeader();
        byte[] header = headerBuilder.toString().getBytes(UTF_8_CHARSET);
        byte[] response = new byte[header.length + body.length];
        System.arraycopy(header, 0, response, 0, header.length);
        System.arraycopy(body, 0, response, header.length, body.length);
        return response;
    }
}
