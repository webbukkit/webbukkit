package org.webbukkit;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.webbukkit.http.HttpContext;
import org.webbukkit.http.HttpHandler;

public class HttpPathDelegator implements HttpHandler {

    private SortedMap<String, Handler> handlers = new TreeMap<String, Handler>(Collections.reverseOrder());
    private HashMap<HttpHandler, Handler> reverseLookup = new HashMap<HttpHandler, Handler>();

    @Override
    public void handle(String path, HttpContext context) throws Exception {
        String relativePath = null;
        HttpHandler handler = null;
        
        // TODO: Optimize HttpHandler-finding by using a real path-aware tree.
        for (Entry<String, Handler> entry : handlers.entrySet()) {
            String key = entry.getKey();
            boolean directoryHandler = key.endsWith("/");
            if (directoryHandler && path.startsWith(entry.getKey()) || !directoryHandler && path.equals(entry.getKey())) {
                relativePath = path.substring(entry.getKey().length());
                handler = entry.getValue().handler;
            }
        }
        if (handler == null) {
            return;
        }
        handler.handle(relativePath, context);
    }

    public void registerHandler(String path, HttpHandler httpHandler) {
        if (handlers.containsKey(path)) {
            throw new InvalidParameterException("The specified path is already registered.");
        }
        Handler handler = reverseLookup.get(httpHandler);
        if (handler == null) {
            handler = new Handler(httpHandler);
            reverseLookup.put(httpHandler, handler);
        }
        handler.paths.add(path);
        handlers.put(path, handler);
    }

    public void unregisterHandler(String path) {
        Handler handler = handlers.remove(path);
        if (handler == null)
            return;
        handler.paths.remove(path);
    }

    public void unregisterHandler(HttpHandler httpHandler) {
        Handler handler = reverseLookup.get(httpHandler);
        if (handler == null)
            return;
        unregisterHandler(handler);
    }

    private void unregisterHandler(Handler handler) {
        reverseLookup.remove(handler.handler);
        for(String path : handler.paths) {
            handlers.remove(path);
        }
    }

    public Map<String, HttpHandler> getRegistrations() {
        HashMap<String, HttpHandler> l = new HashMap<String, HttpHandler>();
        for(Entry<String, Handler> entry : handlers.entrySet()) {
            l.put(entry.getKey(), entry.getValue().handler);
        }
        return l;
    }

    private static class Handler {
        public HttpHandler handler;
        public List<String> paths = new ArrayList<String>();
        
        public Handler(HttpHandler handler) {
            this.handler = handler;
        }
    }
}
