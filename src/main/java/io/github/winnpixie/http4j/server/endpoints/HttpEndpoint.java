package io.github.winnpixie.http4j.server.endpoints;

import io.github.winnpixie.http4j.server.direction.outgoing.HttpResponse;

import java.util.function.Consumer;

public class HttpEndpoint {
    private final String path;
    private final Consumer<HttpResponse> handler;

    public HttpEndpoint(String path, Consumer<HttpResponse> handler) {
        this.path = path;
        this.handler = handler;
    }

    public String getPath() {
        return path;
    }

    public Consumer<HttpResponse> getHandler() {
        return handler;
    }
}
