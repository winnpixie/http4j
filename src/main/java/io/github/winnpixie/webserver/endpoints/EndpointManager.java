package io.github.winnpixie.webserver.endpoints;

import io.github.winnpixie.webserver.endpoints.impl.DefaultEndpoint;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EndpointManager {
    private final List<Endpoint> endpoints = new ArrayList<>();
    private final DefaultEndpoint defaultEndpoint = new DefaultEndpoint();

    @NotNull
    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void register(@NotNull Endpoint endpoint) {
        endpoints.add(endpoint);
    }

    public void unregister(@NotNull Endpoint endpoint) {
        endpoints.remove(endpoint);
    }

    public Endpoint getEndpoint(@NotNull String path) {
        for (Endpoint endpoint : endpoints) {
            if (path.startsWith(endpoint.getPath())) {
                return endpoint;
            }
        }

        return defaultEndpoint;
    }
}
