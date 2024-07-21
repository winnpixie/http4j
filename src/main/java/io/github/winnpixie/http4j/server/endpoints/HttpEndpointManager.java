package io.github.winnpixie.http4j.server.endpoints;

import io.github.winnpixie.http4j.server.endpoints.impl.LocalFileHttpEndpoint;

import java.util.ArrayList;
import java.util.List;

public class HttpEndpointManager {
    private final List<HttpEndpoint> endpoints = new ArrayList<>();
    private final LocalFileHttpEndpoint defaultEndpoint = new LocalFileHttpEndpoint();

    public List<HttpEndpoint> getEndpoints() {
        return endpoints;
    }

    public boolean register(HttpEndpoint endpoint) {
        return endpoints.add(endpoint);
    }

    public boolean unregister(HttpEndpoint endpoint) {
        return endpoints.remove(endpoint);
    }

    public HttpEndpoint getEndpoint(String path) {
        for (HttpEndpoint endpoint : endpoints) {
            if (!path.startsWith(endpoint.getPath())) continue;

            return endpoint;
        }

        return defaultEndpoint;
    }
}
