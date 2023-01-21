package io.github.winnpixie.webserver;

import io.github.winnpixie.webserver.endpoints.EndpointManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

public class HttpServer {
    private final Logger logger = Logger.getLogger(HttpServer.class.getName());
    private int port;
    private File rootDirectory;
    private boolean running;
    private final EndpointManager endpointManager = new EndpointManager();
    private Thread serverThread;

    public HttpServer(int port, @NotNull File rootDirectory) {
        this.port = port;
        this.rootDirectory = rootDirectory;
    }

    public Logger getLogger() {
        return logger;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (running) throw new RuntimeException("Can not change port while server is running!");

        this.port = port;
    }

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

        startThread();
    }

    public void stop() {
        this.running = false;
    }

    private void startThread() {
        this.serverThread = new Thread(() -> {
            try (var srvSocket = new ServerSocket(port)) {
                logger.info("Http Server started at %s:%d"
                        .formatted(srvSocket.getInetAddress().getHostName(), srvSocket.getLocalPort()));

                while (running) {
                    var socket = srvSocket.accept();
                    socket.setSoTimeout(15000);
                    new RequestThread(this, socket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
    }
}
