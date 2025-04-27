package io.github.winnpixie.http4j.server.endpoints;

import io.github.winnpixie.http4j.server.endpoints.impl.FileHttpEndpoint;

import java.util.ArrayList;
import java.util.List;

public class HttpEndpointManager {
    private final FileHttpEndpoint defaultEndpoint = new FileHttpEndpoint();
    private final List<HttpEndpoint> endpoints = new ArrayList<>();

    public FileHttpEndpoint getDefaultEndpoint() {
        return defaultEndpoint;
    }

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
            if (path.startsWith(endpoint.getPath())) return endpoint;
        }

        return defaultEndpoint;
    }
}
