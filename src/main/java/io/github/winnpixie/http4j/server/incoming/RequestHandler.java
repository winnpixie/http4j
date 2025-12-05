package io.github.winnpixie.http4j.server.incoming;

import io.github.winnpixie.http4j.server.outgoing.Response;

import java.io.IOException;

public abstract class RequestHandler {
    private final String path;

    protected RequestHandler(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public abstract Response process(Request request) throws IOException;
}
