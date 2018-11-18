package com.miao.webserver.servlet.impl;

import com.miao.webserver.common.RequestMethod;
import com.miao.webserver.exception.ServletException;
import com.miao.webserver.request.Request;
import com.miao.webserver.response.Response;
import com.miao.webserver.servlet.Servlet;

import java.io.IOException;

public abstract class HttpServlet implements Servlet{

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void service(Request request, Response response) throws ServletException, IOException {
        if (request.getMethod() == RequestMethod.GET) {
            doGet(request, response);
        } else if (request.getMethod() == RequestMethod.POST) {
            doPost(request, response);
        } else if (request.getMethod() == RequestMethod.PUT) {
            doPut(request, response);
        } else if (request.getMethod() == RequestMethod.DELETE) {
            doDelete(request, response);
        }
    }

    protected abstract void doGet(Request request, Response response);
    protected abstract void doPost(Request request, Response response);
    protected abstract void doPut(Request request, Response response);
    protected abstract void doDelete(Request request, Response response);
}
