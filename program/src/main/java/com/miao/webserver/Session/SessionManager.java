package com.miao.webserver.Session;

import static com.miao.webserver.common.Constants.ContextConstant.DEFAULT_SESSION_EXPIRE_TIME;

import com.miao.webserver.context.ServletContext;
import com.miao.webserver.context.WebApplication;
import com.miao.webserver.util.UUIDUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private SessionManager() {
    }

    private ServletContext context = WebApplication.getServletContext();

    private static class SingletonInner{
        private static SessionManager manager = new SessionManager();
    }

    public static SessionManager getManager() {
        return SingletonInner.manager;
    }

    private Map<String, HttpSession> sessionMap = new ConcurrentHashMap<>();

    public HttpSession getSession(String jsessionID) {
        return sessionMap.get(jsessionID);
    }

    public HttpSession createSession() {
        HttpSession session = new HttpSession(UUIDUtil.uuid());
        sessionMap.put(session.getId(), session);
        context.afterSessionCreate(session);
        return session;
    }

    /**
     * 销毁session
     * @param session
     */
    public void invalidateSession(HttpSession session) {
        sessionMap.remove(session.getId());
        context.afterSessionDestroyed(session); //触发监听器
    }

    /**
     * 清空超时的session
     */
    public void cleanIdleSession() {
//        this.sessionMap.entrySet().removeIf(entry -> Duration.between(
//                entry.getValue().getLastAccessed(), Instant.now())
//                .getSeconds() > DEFAULT_SESSION_EXPIRE_TIME);
        Iterator<Map.Entry<String, HttpSession>> iterator = this.sessionMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, HttpSession> entry = iterator.next();
            if (Duration.between(entry.getValue().getLastAccessed(),
                    Instant.now()).getSeconds() > DEFAULT_SESSION_EXPIRE_TIME) {
                context.afterSessionDestroyed(entry.getValue()); //触发监听器
                iterator.remove();
            }
        }
    }
}
