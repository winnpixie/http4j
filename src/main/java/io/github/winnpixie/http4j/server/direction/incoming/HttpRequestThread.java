package io.github.winnpixie.http4j.server.direction.incoming;

import io.github.winnpixie.http4j.server.direction.outgoing.HttpResponse;
import io.github.winnpixie.http4j.shared.HttpResponseStatus;
import io.github.winnpixie.http4j.server.direction.shared.HttpSocketHandler;
import io.github.winnpixie.http4j.server.HttpServer;

import java.net.Socket;

public class HttpRequestThread extends Thread {
    private final HttpServer server;
    private final HttpSocketHandler socketHandler;

    public HttpRequestThread(HttpServer server, Socket socket) {
        this.server = server;
        this.socketHandler = new HttpSocketHandler(socket);

        super.setName("http-srv_%s_%d".formatted(socket.getInetAddress(), System.nanoTime()));
    }

    public HttpServer getServer() {
        return server;
    }

    public HttpSocketHandler getSocketHandler() {
        return socketHandler;
    }

    @Override
    public void run() {
        try (Socket sock = socketHandler.getSocket()) {
            HttpRequest request = new HttpRequest(this);
            request.read();

            HttpResponse response = new HttpResponse(request);
            if (request.getHeader("Host", false).isEmpty() || request.getPath().indexOf('/') > 0) {
                response.setStatus(HttpResponseStatus.BAD_REQUEST);
            } else {
                server.getEndpointManager().getEndpoint(request.getPath()).getHandler().accept(response);
            }
            response.write();

            server.getLogger().info("ip-addr=%s/x-fwd=%s path='%s' status-code=%d user-agent='%s'"
                    .formatted(sock.getInetAddress(), request.getHeader("X-Forwarded-For", false), request.getPath(),
                            response.getStatus().getCode(), request.getHeader("User-Agent", false)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
