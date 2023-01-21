package io.github.winnpixie.webserver;

import org.jetbrains.annotations.NotNull;

import java.net.Socket;

public class RequestThread extends Thread {
    private final HttpServer server;
    private final SocketHandler socketHandler;

    public RequestThread(@NotNull HttpServer server, @NotNull Socket socket) {
        this.server = server;
        this.socketHandler = new SocketHandler(socket);
    }

    @NotNull
    public HttpServer getServer() {
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
            if (request.getHeader("Host", false).isEmpty() || request.getPath().indexOf('/') > 0) {
                response.setStatusCode(400);
                response.setReasonPhrase("Bad Request");
            } else {
                server.getEndpointManager().getEndpoint(request.getPath()).getHandler().accept(response);
            }
            response.write();

            server.getLogger().info("ip-addr=%s path='%s' status-code=%d user-agent='%s'"
                    .formatted(sock.getInetAddress(), request.getPath(), response.getStatusCode(),
                            request.getHeader("User-Agent", false)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
