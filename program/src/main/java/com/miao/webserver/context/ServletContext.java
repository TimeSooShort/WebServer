package com.miao.webserver.context;

import com.miao.webserver.context.holder.FilterHolder;
import com.miao.webserver.context.holder.ServletHolder;
import com.miao.webserver.exception.FilterNotFoundException;
import com.miao.webserver.exception.ServletException;
import com.miao.webserver.exception.ServletNotFoundException;
import com.miao.webserver.filter.Filter;
import com.miao.webserver.servlet.Servlet;
import com.miao.webserver.util.XMLUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.util.AntPathMatcher;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
     *      <url-pattern>/login;/xx;/xx</url-pattern>
     *  </servlet-mapping>
     *  key为url-pattern；value为servlet-name，通过它去servletNameClass找到具体路径
     *
     *  对应关系：一个servlet可以对应多个URL， 一个URL只能对应一个Servlet
     */
    private Map<String, String> servletMaptUrlName;

    /**
     *     <filter-mapping>
     *          <filter-name>LoginFilter</filter-name>
     *          <url-pattern>/**;/xx;/xx</url-pattern>
     *      </filter-mapping>
     * key: url-pattern ; value: filter-name
     *
     * 由于Filter起到拦截预处理的功能，那么例如一个请求可能需要多道Filter进行处理，
     * 所以存在相同url映射不同Filter的情况。
     * 对应关系：多对多的关系，一个URL可以对应多个Filter， 多个URL对应一个Filter
     */
    private Map<String, List<String>> filterMapUrlName;

    /**
     *     <filter>
     *          <filter-name>LogFilter</filter-name>
     *          <filter-class>com.xxx.xxx.Filter</filter-class>
     *      </filter>
     * key: filter-name ; value: FilterHolder
     */
    private Map<String, FilterHolder> filterNameClass;

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
        this.filterMapUrlName = new HashMap<>();
        this.filterNameClass = new HashMap<>();
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

        filterNameClass.values().forEach(filterHolder -> {
            if (filterHolder.getFilter() != null) {
                filterHolder.getFilter().destroy();
            }
        });
    }

    /**
     * 解析web.xml
     */
    private void parseWebXml() throws DocumentException {
        Document document = XMLUtil.getDocument(ServletContext.class.getResourceAsStream("/web.xml"));
        Element root = document.getRootElement();

        // 解析servlet
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

        //解析Filter
        List<Element> filters = root.elements("filter");
        for (Element filter : filters) {
            String filterName = filter.elementText("filter-name");
            String filterClass = filter.elementText("filter-class");
            this.filterNameClass.put(filterName, new FilterHolder(filterClass));
        }

        // 一个url-pattern标签可能存在多个url，每个url又可能对应多个Filter
        List<Element> filterMaps = root.elements("filter-mapping");
        for (Element filterMap : filterMaps) {
            String filterName = filterMap.elementText("filter-name");
            List<Element> filterUrls = filterMap.elements("url-pattern");
            for (Element filterUrl : filterUrls) {
                List<String> nameList = this.filterMapUrlName.get(filterUrl.getText());
                if (nameList == null) {
                    nameList = new ArrayList<>();
                    this.filterMapUrlName.put(filterUrl.getText(), nameList);
                }
                nameList.add(filterName);
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

        // 找寻已有url集合中最匹配当前的url
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

    /**
     * 通过url获得对应Filter实例集合
     * @param url
     * @return
     * @throws ServletException
     */
    public List<Filter> mapFilter(String url) throws ServletException {
//        List<String> filterUrlMatched = new ArrayList<>();
//        Set<String> urlSet = this.filterMapUrlName.keySet();
//        for (String urlPattern : urlSet) {
//            if (matcher.match(urlPattern, url)) {
//                filterUrlMatched.add(urlPattern);
//            }
//        }
//        List<Filter> result = null;
//        if (!filterUrlMatched.isEmpty()) {
//            result = new ArrayList<>();
//            Set<String> nameSet = new TreeSet<>();
//            for (String urlPattern : filterUrlMatched) {
//                nameSet.addAll(this.filterMapUrlName.get(urlPattern));
//            }
//            for (String name : nameSet) {
//                result.add(getFilterInstance(name));
//            }
//        }
//        return result;
        List<Filter> result = new ArrayList<>();
        List<String> filterNames = this.filterMapUrlName.keySet().stream()
                .filter(urlPattern -> matcher.match(urlPattern, url))
                .flatMap(urlPattern -> this.filterMapUrlName.get(urlPattern).stream())
                .collect(Collectors.toList());
        if (!filterNames.isEmpty()) {
            for (String filterName : filterNames)
                result.add(getFilterInstance(filterName));
        }
        return result;
    }

    /**
     * 通过filter name来获得Filter实例
     * @param name filter name
     * @return Filter实例
     * @throws ServletException
     */
    private Filter getFilterInstance(String name) throws ServletException {
        FilterHolder holder = this.filterNameClass.get(name);
        if (holder == null) {
            throw new FilterNotFoundException();
        }
        Filter filter = holder.getFilter();
        if (filter == null) {
            try {
                String classPath = holder.getFilterClass();
                filter = (Filter) Class.forName(classPath).newInstance();
                filter.init();
                holder.setFilter(filter);
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return filter;
    }
}
