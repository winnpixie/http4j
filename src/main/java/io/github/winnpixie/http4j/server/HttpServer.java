package io.github.winnpixie.http4j.server;

import io.github.winnpixie.http4j.server.handlers.PathHandlers;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer {
    private final Logger logger = Logger.getLogger(HttpServer.class.getName());
    private final PathHandlers pathHandlers = new PathHandlers();

    private int port;
    private int connectionLimit;
    private int contentLengthLimit = 1024 * 1024 * 1024; // 1G

    private boolean running;
    private HttpServerThread serverThread;

    public HttpServer(int port, int connectionLimit) {
        this.port = port;
        this.connectionLimit = connectionLimit;
    }

    public HttpServer(int port) {
        this(port, 64);
    }

    public Logger getLogger() {
        return logger;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (running) {
            throw new IllegalStateException("Cannot assign port while server is running");
        }

        this.port = port;
    }

    public int getConnectionLimit() {
        return connectionLimit;
    }

    public void setConnectionLimit(int connectionLimit) {
        if (running) {
            throw new IllegalStateException("Cannot set connection limit while server is running");
        }

        this.connectionLimit = connectionLimit;
    }

    public int getContentLengthLimit() {
        return contentLengthLimit;
    }

    public void setContentLengthLimit(int contentLengthLimit) {
        this.contentLengthLimit = contentLengthLimit;
    }

    public boolean isRunning() {
        return running;
    }

    public PathHandlers getPathHandlers() {
        return pathHandlers;
    }

    public void start() {
        if (running) {
            throw new IllegalStateException("Cannot start server while it is already running");
        }

        this.running = true;
        this.serverThread = new HttpServerThread(this);
        serverThread.start();
    }

    public void stop() {
        this.running = false;

        try {
            serverThread.join(10000);
        } catch (InterruptedException ie) {
            logger.log(Level.SEVERE, "Error waiting for thread to finish", ie);

            serverThread.interrupt();
        }
    }
}
