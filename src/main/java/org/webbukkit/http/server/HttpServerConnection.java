package org.webbukkit.http.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webbukkit.http.HttpContext;
import org.webbukkit.http.HttpField;
import org.webbukkit.http.HttpRequest;
import org.webbukkit.http.HttpResponse;

public class HttpServerConnection extends Thread {
    protected static final Logger log = Logger.getLogger("Minecraft");

    private static Pattern requestHeaderLine = Pattern.compile("^(\\S+)\\s+(\\S+)\\s+HTTP/(.+)$");
    private static Pattern requestHeaderField = Pattern.compile("^([^:]+):\\s*(.+)$");
    
    private Socket socket;
    private HttpServer server;
    
    private PrintStream printOut;
    private StringWriter sw = new StringWriter();
    private Matcher requestHeaderLineMatcher;
    private Matcher requestHeaderFieldMatcher;

    public HttpServerConnection(Socket socket, HttpServer server) {
        this.socket = socket;
        this.server = server;
    }

    private final static void readLine(InputStream in, StringWriter sw) throws IOException {
        int readc;
        while((readc = in.read()) > 0) {
            char c = (char)readc;
            if (c == '\n')
                break;
            else if (c != '\r')
                sw.append(c);
        }
    }
    
    private final String readLine(InputStream in) throws IOException {
        readLine(in, sw);
        String r = sw.toString();
        sw.getBuffer().setLength(0);
        return r;
    }

    private final boolean readRequestHeader(InputStream in, HttpRequest request) throws IOException {
        String statusLine = readLine(in);
        
        if (statusLine == null)
            return false;
        
        if (requestHeaderLineMatcher == null) {
            requestHeaderLineMatcher = requestHeaderLine.matcher(statusLine);
        } else {
            requestHeaderLineMatcher.reset(statusLine);
        }
        
        Matcher m = requestHeaderLineMatcher;
        if (!m.matches())
            return false;
        request.method = m.group(1);
        request.path = m.group(2);
        request.version = m.group(3);

        String line;
        while (!(line = readLine(in)).equals("")) {
            if (requestHeaderFieldMatcher == null) {
                requestHeaderFieldMatcher = requestHeaderField.matcher(line);
            } else {
                requestHeaderFieldMatcher.reset(line);
            }
            
            m = requestHeaderFieldMatcher;
            // Warning: unknown lines are ignored.
            if (m.matches()) {
                String fieldName = m.group(1);
                String fieldValue = m.group(2);
                // TODO: Does not support duplicate field-names.
                request.fields.put(fieldName, fieldValue);
            }
        }
        return true;
    }

    public static final void writeResponseHeader(PrintStream out, HttpResponse response) throws IOException {
        out.append("HTTP/");
        out.append(response.version);
        out.append(" ");
        out.append(String.valueOf(response.status.getCode()));
        out.append(" ");
        out.append(response.status.getText());
        out.append("\r\n");
        for (Entry<String, String> field : response.fields.entrySet()) {
            out.append(field.getKey());
            out.append(": ");
            out.append(field.getValue());
            out.append("\r\n");
        }
        out.append("\r\n");
        out.flush();
    }
    
    public final void writeResponseHeader(HttpResponse response) throws IOException {
        writeResponseHeader(printOut, response);
    }

    public void run() {
        try {
            if (socket == null)
                return;
            socket.setSoTimeout(5000);
            InputStream in = socket.getInputStream();
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream(), 40960);
            
            printOut = new PrintStream(out, false);
            while (true) {
                HttpRequest request = new HttpRequest();
                
                if (!readRequestHeader(in, request)) {
                    socket.close();
                    return;
                }
                
                long bound = -1;
                BoundInputStream boundBody = null;
                {
                    String contentLengthStr = request.fields.get(HttpField.ContentLength);
                    if (contentLengthStr != null) {
                        try {
                            bound = Long.parseLong(contentLengthStr);
                        } catch (NumberFormatException e) {
                        }
                        if (bound >= 0) {
                            request.body = boundBody = new BoundInputStream(in, bound);
                        } else {
                            request.body = in;
                        }
                    }
                }

                NonClosableOutputStream nonClosableResponseBody = new NonClosableOutputStream(out);
                final HttpResponse response = new HttpResponse(this, nonClosableResponseBody);

                HttpContext context = new HttpContext(request, response, this);
                try {
                    server.handler.handle(request.path, context);
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    log.log(Level.SEVERE, "HttpHandler '" + server.handler + "' has thown an exception", e);
                    if (socket != null) {
                        out.flush();
                        socket.close();
                    }
                    return;
                }
                // Does not do anything, but looks more sane.
                nonClosableResponseBody.close();

                if (bound > 0 && boundBody.skip(bound) < bound) {
                    //socket.close();
                    //return;
                }
                
                String connection = response.fields.get("Connection");
                String contentLength = response.fields.get("Content-Length");
                if (contentLength == null && connection == null) {
                    response.fields.put("Content-Length", "0");
                    OutputStream responseBody = response.getBody();

                    // The HttpHandler has already send the headers and written to the body without setting the Content-Length.
                    if (responseBody == null) {
                        out.flush();
                        socket.close();
                        return;
                    }
                }

                if (connection != null && connection.equals("close")) {
                    out.flush();
                    socket.close();
                    return;
                }

                out.flush();
            }
        } catch (IOException e) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                }
            }
            return;
        } catch (Exception e) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                }
            }
            log.log(Level.SEVERE, "Exception while handling request: ", e);
            e.printStackTrace();
            return;
        }
    }
}
