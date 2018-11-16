package com.miao.webserver.request;

import com.miao.webserver.common.Constants;
import com.miao.webserver.common.RequestMethod;
import com.miao.webserver.cookie.Cookie;
import com.miao.webserver.exception.RequestInvalidException;
import com.miao.webserver.exception.RequestParseException;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Request {

    private RequestMethod method;
    private String url;
    private Map<String, List<String>> params;
    private Map<String, List<String>> headers;
    private Cookie[] cookies;

    public Request(byte[] data) throws RequestInvalidException, RequestParseException {
        String[] segment = null;
        try {
            segment = URLDecoder.decode(
                    new String(data, Constants.UTF_8_CHARSET), Constants.UTF_8)
                    .split(Constants.CRLF);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log.info("请求数据读取完毕");
        log.info("请求报文：{}", Arrays.toString(segment));
        if (segment == null || segment.length <= 1) {
            throw new RequestInvalidException();
        }
        // 开始解析请求报文
        try {
            parseRequestLine(segment);
            if (headers.containsKey("Content-Length") &&
                    !headers.get("Content-Length").get(0).equals("0")) {
                parseBody(segment[segment.length-1]);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RequestParseException();
        }
    }

    /**
     * 获取url
     * @return
     */
    public String getServletPath() {
        return url;
    }

    /**
     * 获取参数值
     * @param key 参数名
     * @return 参数值
     */
    public String getParameter(String key) {
        List<String> paramList = this.params.get(key);
        if (paramList == null) return null;
        return paramList.get(0);
    }

    /**
     * 解析请求行
     * @param segment 请求数据
     */
    private void parseRequestLine(String[] segment) {
        log.info("解析请求行");
        //请求方法
        String requestLine = segment[0];
        String[] requestLineSlice = requestLine.split(Constants.BLANK);
        this.method = RequestMethod.valueOf(requestLineSlice[0]);
        log.debug("method: {}", this.method);

        // 请求URI
        String URI = requestLineSlice[1];
        String[] uriSlice = URI.split("\\?");  // 正则表达式中 \? 代表匹配问好符
        this.url = uriSlice[0];
        log.debug("url: {}", this.url);

        // 请求行参数
        if (uriSlice.length > 1) {
            log.info("解析来自请求行的参数");
            parseParams(uriSlice[1]);
        }
        log.debug("params: {}", this.params);

        parseHeader(segment);
    }

    /**
     * 解析请求参数,来自请求行或者请求实体
     * @param paramString
     */
    private void parseParams(String paramString) {
        if (this.params == null) params = new HashMap<>();
        String[] paramKVs = paramString.split("&");
        for (String paramKV : paramKVs) {
            String[] kv = paramKV.split("=");
            String key = kv[0];
            String[] values = kv[1].split(",");
            this.params.put(key, Arrays.asList(values));
        }
        log.debug("params:{}", params);
    }

    /**
     * 解析请求头
     * @param segment
     */
    private void parseHeader(String[] segment) {
        log.info("开始解析请求头");
        this.headers = new HashMap<>();
        String header;
        for (int i = 1; i < segment.length; i++) {
            header = segment[i];
            if (header.equals("")) break;
            int colonIndex = header.indexOf(":");
            String headName = header.substring(0, colonIndex);
            String[] values = header.substring(colonIndex+2).split(",");
            headers.put(headName, Arrays.asList(values));
        }
        log.debug("headers:{}", headers);

        // 解析Cookie
        if (headers.containsKey("Cookie")) {
            String[] cookieArray = headers.get("Cookie").get(0).split(";");
            this.cookies = new Cookie[cookieArray.length];
            for (int i = 0; i < cookieArray.length; i++) {
                String[] cookieKeyValue = cookieArray[i].split("=");
                this.cookies[i] = new Cookie(cookieKeyValue[0], cookieKeyValue[1]);
            }
            headers.remove("Cookie");
        } else {
            this.cookies = new Cookie[0];
        }
        log.info("Cookies:{}", Arrays.toString(cookies));

    }

    /**
     * 解析请求实体
     */
    private void parseBody(String body) {
        log.info("解析请求实体");
        byte[] bytes = body.getBytes(Constants.UTF_8_CHARSET);
        List<String> lengths = this.headers.get("Content-Length");
        if (lengths != null) {
            int length = Integer.valueOf(lengths.get(0));
            log.info("length:{}", length);
            log.info("解析来自请求实体的参数");
            parseParams(new String(bytes, 0, Math.min(length, bytes.length), Constants.UTF_8_CHARSET));
        } else {
            log.info("没有Content-Length，尝试解析body字符串中的参数");
            parseParams(body.trim());
        }
    }
}
