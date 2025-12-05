package io.github.winnpixie.http4j.server;

import io.github.winnpixie.http4j.server.incoming.RequestHandlers;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer {
    private final Logger logger = Logger.getLogger(HttpServer.class.getName());
    private final RequestHandlers requestHandlers = new RequestHandlers();
    private final HttpServerThread serverThread;

    private int port;
    private boolean running;

    public HttpServer(int port) {
        this.port = port;

        this.serverThread = new HttpServerThread(this);
    }

    public Logger getLogger() {
        return logger;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (running) {
            throw new IllegalStateException("Cannot set port while running.");
        }

        this.port = port;
    }

    public boolean isRunning() {
        return running;
    }

    public RequestHandlers getRequestHandlers() {
        return requestHandlers;
    }

    public void start() {
        this.running = true;

        serverThread.start();
    }

    public void stop() {
        this.running = false;

        try {
            serverThread.join();
        } catch (InterruptedException ie) {
            logger.log(Level.SEVERE, "Error waiting for thread to finish.", ie);

            serverThread.interrupt();
        }
    }
}
