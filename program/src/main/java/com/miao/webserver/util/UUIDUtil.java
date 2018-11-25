package com.miao.webserver.util;

import java.util.UUID;

/**
 * 生成一个id作为用户的标识，用于获取用户的session
 */
public class UUIDUtil {
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
