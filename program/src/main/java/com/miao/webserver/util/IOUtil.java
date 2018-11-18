package com.miao.webserver.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 读取目标文件的工具
 */
@Slf4j
public class IOUtil {

    public static byte[] getBytesFromFile(String fileName) throws IOException {
        InputStream input = IOUtil.class.getResourceAsStream(fileName);
        if (input == null) {
            log.info("文件不存在：{}", fileName);
            throw new FileNotFoundException();
        }
        log.info("读取文件开始:{}", fileName);
        // ByteArrayOutputStream通过不断扩充数组来将全部数据写到其内部的byte buf[]中
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = -1;
        if ((len = input.read(data)) != -1) {
            output.write(data, 0, len);
        }
        output.close();
        input.close();
        return output.toByteArray(); // 返回ByteArrayOutputStream内部发 buf 数组
    }
}
