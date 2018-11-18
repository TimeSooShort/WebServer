package com.miao.webserver.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.InputStream;

/**
 * 解析XML工具类
 */
public class XMLUtil {

    public static Document getDocument(InputStream input) throws DocumentException {
        SAXReader reader = new SAXReader();
        return reader.read(input);
    }
}
