package io.github.winnpixie.http4j.server.incoming;

import io.github.winnpixie.http4j.server.HttpServer;
import io.github.winnpixie.http4j.server.endpoints.HttpEndpoint;
import io.github.winnpixie.http4j.server.outgoing.HttpResponse;
import io.github.winnpixie.http4j.shared.HttpStatus;

import java.net.Socket;

public class HttpRequestThread extends Thread {
    private final HttpServer server;
    private final Socket socket;

    public HttpRequestThread(HttpServer server, Socket socket) {
        this.server = server;
        this.socket = socket;

        super.setName("http-srv_%s_%d".formatted(socket.getInetAddress(), System.nanoTime()));
    }

    public HttpServer getServer() {
        return server;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try (Socket sock = socket) {
            HttpRequest request = new HttpRequest(this);
            request.read();

            HttpResponse response = new HttpResponse(request);
            if (request.getHeader("Host", false).isEmpty() || request.getPath().indexOf('/') > 0) {
                response.setStatus(HttpStatus.BAD_REQUEST);
            } else {
                HttpEndpoint endpoint = server.getEndpointManager().getEndpoint(request.getPath());
                if (endpoint != null) endpoint.handle(response);
            }
            response.write();

            server.getLogger().info("ip-addr=%s/x-fwd=%s path='%s' status-code=%d user-agent='%s'"
                    .formatted(sock.getInetAddress().getHostAddress(), request.getHeader("X-Forwarded-For", false),
                            request.getPath(), response.getStatus().getCode(), request.getHeader("User-Agent", false)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
