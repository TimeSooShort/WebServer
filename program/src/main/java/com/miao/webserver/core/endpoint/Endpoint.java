package com.miao.webserver.core.endpoint;

import org.springframework.util.StringUtils;

public abstract class Endpoint {

    /**
     * 启动服务器
     * @param port 端口
     */
    public abstract void start(int port);

    /**
     * 关闭服务器
     */
    public abstract void close();

    /**
     * 通过反射获取相应Endpoint的实例
     * @param connector bio,nio,aio
     * @return Endpoint
     */
    public static Endpoint getEndpoint(String connector) {
        StringBuilder path = new StringBuilder();
        path.append("com.miao.webserver.core.endpoint")
                .append(".").append(connector).append(".")
                .append(StringUtils.capitalize(connector))
                .append("Endpoint");
        try {
            return (Endpoint) Class.forName(path.toString()).newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("connector : " + connector + " is not suitable");
    }
}
