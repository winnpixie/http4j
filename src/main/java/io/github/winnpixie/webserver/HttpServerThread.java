package io.github.winnpixie.webserver;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpServerThread extends Thread {
    private final HttpServer server;

    public HttpServerThread(@NotNull HttpServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try (var srvSocket = new ServerSocket(server.getPort())) {
            server.getLogger().info("Http Server started at %s:%d"
                    .formatted(srvSocket.getInetAddress().getHostName(), srvSocket.getLocalPort()));

            while (server.isRunning()) {
                var socket = srvSocket.accept();
                socket.setSoTimeout(15000);
                new RequestThread(server, socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
