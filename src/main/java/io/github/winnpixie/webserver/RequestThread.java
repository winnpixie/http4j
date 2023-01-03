package io.github.winnpixie.webserver;

import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public class RequestThread extends Thread {
    private final Server server;
    private final SocketHandler socketHandler;

    public RequestThread(@NotNull Server server, @NotNull Socket socket) {
        this.server = server;
        this.socketHandler = new SocketHandler(socket);
    }

    @NotNull
    public Server getServer() {
        return server;
    }

    @NotNull
    public SocketHandler getSocketHandler() {
        return socketHandler;
    }

    @Override
    public void run() {
        try (var sock = socketHandler.getSocket()) {
            var request = new Request(this);
            request.read();

            var response = new Response(request);
            response.prepare();
            response.write();

            System.out.printf("%s '%s' (%d) [%s]\n", sock.getInetAddress(), request.getPath(),
                    response.getResponseCode(), request.getHeader("User-Agent", false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
