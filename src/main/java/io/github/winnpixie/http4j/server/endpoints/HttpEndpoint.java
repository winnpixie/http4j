package io.github.winnpixie.http4j.server.endpoints;

import io.github.winnpixie.http4j.server.outgoing.HttpResponse;

public abstract class HttpEndpoint {
    private final String path;

    public HttpEndpoint(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public abstract void handle(HttpResponse response);
}
