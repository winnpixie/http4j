package io.github.winnpixie.webserver.endpoints;

import io.github.winnpixie.webserver.direction.outgoing.Response;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Endpoint {
    private final String path;
    private final Consumer<Response> handler;

    public Endpoint(@NotNull String path, @NotNull Consumer<Response> handler) {
        this.path = path;
        this.handler = handler;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public Consumer<Response> getHandler() {
        return handler;
    }
}
