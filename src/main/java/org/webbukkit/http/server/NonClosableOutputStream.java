package org.webbukkit.http.server;

import java.io.IOException;
import java.io.OutputStream;

public class NonClosableOutputStream extends OutputStream {
    public OutputStream baseStream;
    public boolean isClosed = false;

    public NonClosableOutputStream(OutputStream baseStream) {
        this.baseStream = baseStream;
    }

    @Override
    public void write(int b) throws IOException {
        baseStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        baseStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        baseStream.write(b, off, len);
    }

    public OutputStream getBaseStream() {
        return baseStream;
    }

    public void setBaseStream(OutputStream baseStream) {
        this.baseStream = baseStream;
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }
}
