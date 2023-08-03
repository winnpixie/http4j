package io.github.winnpixie.webserver;

import io.github.winnpixie.webserver.direction.incoming.RequestThread;
import org.jetbrains.annotations.NotNull;

import javax.net.ServerSocketFactory;
import java.io.IOException;

public class HttpServerThread extends Thread {
    private final HttpServer server;

    public HttpServerThread(@NotNull HttpServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try (var srvSocket = ServerSocketFactory.getDefault().createServerSocket(server.getPort())) {
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
