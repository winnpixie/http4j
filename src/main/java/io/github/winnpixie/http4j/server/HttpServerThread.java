package io.github.winnpixie.http4j.server;

import io.github.winnpixie.http4j.server.direction.incoming.HttpRequestThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServerThread extends Thread {
    private final HttpServer server;

    public HttpServerThread(HttpServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try (ServerSocket srvSocket = new ServerSocket(server.getPort())) {
            server.getLogger().info("Http Server started at %s:%d"
                    .formatted(srvSocket.getInetAddress().getHostName(), srvSocket.getLocalPort()));

            while (server.isRunning()) {
                Socket socket = srvSocket.accept();
                socket.setSoTimeout(15000);

                new HttpRequestThread(server, socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
