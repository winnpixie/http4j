package io.github.winnpixie.http4j.server;

import io.github.winnpixie.http4j.server.endpoints.HttpEndpointManager;

import java.io.File;
import java.util.logging.Logger;

public class HttpServer {
    // TODO: Create a custom logger implementation so as to not produce extraneous output.
    private final Logger logger = Logger.getLogger(HttpServer.class.getName());
    private int port;
    private File root;
    private boolean running;
    private final HttpEndpointManager endpointManager = new HttpEndpointManager();
    private final HttpServerThread serverThread;

    public HttpServer(int port, File root) {
        this.port = port;
        this.root = root;

        this.serverThread = new HttpServerThread(this);
    }

    public Logger getLogger() {
        return logger;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (running) throw new RuntimeException("Can not change port while server is running.");

        this.port = port;
    }

    public File getRoot() {
        return root;
    }

    public void setRoot(File root) {
        this.root = root;
    }

    public boolean isRunning() {
        return running;
    }

    public HttpEndpointManager getEndpointManager() {
        return endpointManager;
    }

    public void start() {
        this.running = true;

        serverThread.start();
    }

    public void stop() {
        this.running = false;

        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
