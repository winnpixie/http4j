package io.github.winnpixie.httpsrv.endpoints;

import io.github.winnpixie.httpsrv.direction.outgoing.Response;

import java.util.function.Consumer;

public class Endpoint {
    private final String path;
    private final Consumer<Response> handler;

    public Endpoint(String path, Consumer<Response> handler) {
        this.path = path;
        this.handler = handler;
    }

    public String getPath() {
        return path;
    }

    public Consumer<Response> getHandler() {
        return handler;
    }
}
