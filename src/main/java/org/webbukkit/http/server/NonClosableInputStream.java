package org.webbukkit.http.server;

import java.io.IOException;
import java.io.InputStream;

public class NonClosableInputStream extends InputStream {
    public InputStream baseStream;
    public boolean isClosed = false;
    public NonClosableInputStream(InputStream baseStream) {
        this.baseStream = baseStream;
    }
    @Override
    public int read() throws IOException {
        return baseStream.read();
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        return baseStream.read(b);
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return baseStream.read(b, off, len);
    }
    
    @Override
    public synchronized void reset() throws IOException {
        baseStream.reset();
    }
    
    @Override
    public long skip(long n) throws IOException {
        return baseStream.skip(n);
    }
    
    public InputStream getBaseStream() {
        return baseStream;
    }
    
    public void setBaseStream(InputStream baseStream) {
        this.baseStream = baseStream;
    }
    
    @Override
    public int available() throws IOException {
        return baseStream.available();
    }
    
    @Override
    public synchronized void mark(int readlimit) {
        baseStream.mark(readlimit);
    }
    
    @Override
    public boolean markSupported() {
        return baseStream.markSupported();
    }
    
    @Override
    public void close() throws IOException {
        isClosed = true;
    }
}
