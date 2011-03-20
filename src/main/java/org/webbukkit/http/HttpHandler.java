package org.webbukkit.http;


public interface HttpHandler {
    void handle(String path, HttpContext context) throws Exception;
}
