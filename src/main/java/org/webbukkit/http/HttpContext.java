package org.webbukkit.http;

import org.webbukkit.http.server.HttpServerConnection;

public class HttpContext {
    public HttpRequest request;
    public HttpResponse response;
    public HttpServerConnection connection;
    
    public HttpContext(HttpRequest request, HttpResponse response, HttpServerConnection connection) {
        this.request = request;
        this.response = response;
        this.connection = connection;
    }
}
