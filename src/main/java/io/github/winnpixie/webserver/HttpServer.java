package io.github.winnpixie.webserver;

import io.github.winnpixie.webserver.endpoints.EndpointManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public class HttpServer {
    private final Logger logger = Logger.getLogger(HttpServer.class.getName());
    private int port;
    private File rootDirectory;
    private boolean running;
    private final EndpointManager endpointManager = new EndpointManager();
    private final HttpServerThread serverThread;

    public HttpServer(int port, @NotNull File rootDirectory) {
        this.port = port;
        this.rootDirectory = rootDirectory;

        this.serverThread = new HttpServerThread(this);
    }

    @NotNull
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

    @NotNull
    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(@NotNull File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public boolean isRunning() {
        return running;
    }

    @NotNull
    public EndpointManager getEndpointManager() {
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
            throw new RuntimeException(e);
        }
    }
}
