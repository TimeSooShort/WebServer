package com.miao.webserver.context;

import com.miao.webserver.context.holder.ServletHolder;
import com.miao.webserver.exception.ServletNotFoundException;
import com.miao.webserver.servlet.Servlet;
import com.miao.webserver.util.XMLUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.util.AntPathMatcher;

import java.util.*;

import static com.miao.webserver.common.Constants.ContextConstant.DEFAULT_SERVLET_NAME;

/**
 * 该类在初始化时启动，解析web.xml数据
 */
public class ServletContext {

    /**
     * web.xml中：
     *   <servlet>
     *      <servlet-name>LoginServlet</servlet-name>
     *      <servlet-class>com.miao.login.LoginServlet</servlet-class>
     *  </servlet>
     *  key为：servlet-name；
     *  value为：ServletHolder,有两个属性，一个是servlet-class的值，
     *  另一个是对应的Servlet实例只有当用户真正请求该实例的时候在初始化它
     */
    private Map<String, ServletHolder> servletNameClass;

    /**
     * web.xml中：
     *  <servlet-mapping>
     *      <servlet-name>LoginServlet</servlet-name>
     *      <url-pattern>/login</url-pattern>
     *  </servlet-mapping>
     *  key为url-pattern；value为servlet-name，通过它去servletNameClass找到具体路径
     */
    private Map<String, String> servletMaptUrlName;

    /**
     * 利用Spring的路径匹配器
     */
    private AntPathMatcher matcher;

    public ServletContext() throws DocumentException {
        init();
    }

    /**
     * 初始化
     */
    private void init() throws DocumentException {
        this.servletNameClass = new HashMap<>();
        this.servletMaptUrlName = new HashMap<>();
        parseWebXml();
    }

    /**
     * 关闭前调用，这里会调用servlet，filter，listener的destroy方法
     */
    public void destroy() {
        servletNameClass.values().forEach(servletHolder -> {
            if (servletHolder.getServlet() != null) {
                servletHolder.getServlet().destroy();
            }
        });
    }

    /**
     * 解析web.xml
     */
    private void parseWebXml() throws DocumentException {
        Document document = XMLUtil.getDocument(ServletContext.class.getResourceAsStream("/web.xml"));
        Element root = document.getRootElement();

        List<Element> servlets = root.elements("servlet");
        for (Element servletElement : servlets) {
            String key = servletElement.elementText("servlet-name");
            String value = servletElement.elementText("servlet-class");
            // 这里ServletHolder有两个属性，一个是servlet-class的值，另一个是对应的Servlet实例
            // 只有当用户真正请求该实例的时候在初始化它
            this.servletNameClass.put(key, new ServletHolder(value));
        }

        List<Element> servletMaps = root.elements("servlet-mapping");
        for (Element servletMapEle : servletMaps) {
            List<Element> urlPatterns = servletMapEle.elements("url-pattern");
            String value = servletMapEle.elementText("servlet-name");
            for (Element urlPattern : urlPatterns) {
                this.servletMaptUrlName.put(urlPattern.getText(), value);
            }
        }
    }

    /**
     * 通过web.xml中servlet标签下的url-pattern标签的值来获取servlet-name的值
     * 该值存储在Map servletMaptUrlName中，若其不存在通过Spring提供的路径匹配器
     * 找寻到现存的url-pattern值中匹配度最高的，若无匹配则默认为DefaultServlet
     * @param url
     * @return
     */
    public Servlet mapServlet(String url) throws ServletNotFoundException {
        String servletName = servletMaptUrlName.get(url);
        if (servletName != null) {
            return getServletInstance(servletName);
        }

        List<String> matchedUrlInMap = new ArrayList<>();
        Set<String> urlPatterns = servletMaptUrlName.keySet();
        for (String pattern : urlPatterns) {
            if (matcher.match(pattern, url)) {
                matchedUrlInMap.add(pattern);
            }
        }
        if (!matchedUrlInMap.isEmpty()) {
            Comparator<String> patternCompara = matcher.getPatternComparator(url);
            Collections.sort(matchedUrlInMap, patternCompara);
            String bestMatch = matchedUrlInMap.get(0);
            return getServletInstance(bestMatch);
        }
        return getServletInstance(DEFAULT_SERVLET_NAME);
    }

    /**
     * 通过servlet-name的值来获取到对应Servlet的实例
     * @param servletName servlet-name
     * @return 对应Servlet的实例
     */
    private Servlet getServletInstance(String servletName) throws ServletNotFoundException {
        ServletHolder servletHolder = servletNameClass.get(servletName);
        if (servletHolder == null) {
            throw new ServletNotFoundException();
        }
        if (servletHolder.getServlet() == null) {
            try {
                Servlet servlet = (Servlet) Class.forName(servletHolder.getServletUrl()).newInstance();
                servlet.init(); //调用其init方法，之在初次初始化时调用该方法
                servletHolder.setServlet(servlet);
                return servlet;
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return servletHolder.getServlet();
    }
}
