package io.github.winnpixie.http4j.server.handlers;

import io.github.winnpixie.http4j.server.incoming.HttpRequest;
import io.github.winnpixie.http4j.server.outgoing.HttpResponse;
import io.github.winnpixie.http4j.shared.throwables.HttpException;

public abstract class PathHandler {
    private final String path;

    protected PathHandler(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public abstract HttpResponse process(HttpRequest request) throws HttpException;
}
