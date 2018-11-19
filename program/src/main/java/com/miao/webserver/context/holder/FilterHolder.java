package com.miao.webserver.context.holder;

import com.miao.webserver.filter.Filter;
import lombok.Data;

/**
 * filter class 到 filter对象的映射类
 */
@Data
public class FilterHolder {
    private Filter filter;
    private String filterClass;

    public FilterHolder(String filterClass) {
        this.filterClass = filterClass;
    }
}
