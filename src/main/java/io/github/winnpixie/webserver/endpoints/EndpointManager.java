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

    public boolean register(@NotNull Endpoint endpoint) {
        return endpoints.add(endpoint);
    }

    public boolean unregister(@NotNull Endpoint endpoint) {
        return endpoints.remove(endpoint);
    }

    public Endpoint getEndpoint(@NotNull String path) {
        for (Endpoint endpoint : endpoints) {
            if (!path.startsWith(endpoint.getPath())) continue;

            return endpoint;
        }

        return defaultEndpoint;
    }
}
