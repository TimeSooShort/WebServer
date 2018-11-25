package com.miao.webserver.Session;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class HttpSession {

    private String id;
    private Map<String, Object> attributes;
    private boolean isValid;
    private Instant lastAccessed; // 最近访问时间

    public HttpSession(String id) {
        this.id = id;
        this.attributes = new HashMap<>();
        this.isValid = true;
        this.lastAccessed = Instant.now();
    }

    /**
     * 使当前session失效，清除session数据，删除SessionManager的map中该session
     */
    public synchronized void invalidate() {
        this.isValid = false;
        this.attributes.clear();
        SessionManager.getManager().invalidateSession(this);
    }

    public synchronized Object getAttribute(String key) {
        if (isValid) {
            this.lastAccessed = Instant.now();
            return attributes.get(key);
        }
        throw new IllegalStateException("session is inValidate");
    }

    public synchronized void setAttribute(String key, Object value) {
        if (isValid) {
            this.lastAccessed = Instant.now();
            attributes.put(key, value);
        } else {
            throw new IllegalStateException("session is inValidate");
        }
    }

    public synchronized void removeAttribute(String key) {
        attributes.remove(key);
    }

    public String getId() {
        return id;
    }

    public Instant getLastAccessed() {
        return lastAccessed;
    }
}
