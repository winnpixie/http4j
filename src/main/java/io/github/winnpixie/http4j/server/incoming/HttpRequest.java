package io.github.winnpixie.http4j.server.incoming;


import io.github.winnpixie.http4j.server.HttpServer;
import io.github.winnpixie.http4j.shared.HttpMethod;

import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final HttpServer server;
    private final SocketChannel channel;

    private final HttpMethod method;
    private final String path;
    private final String protocol;
    private final String query;
    private final Map<String, String> headers;
    private final byte[] body;

    private Map<String, String> queryCache; // lazily loaded

    private HttpRequest(Builder builder) {
        this.server = builder.server;
        this.channel = builder.channel;

        this.method = builder.method;
        this.path = builder.path;
        this.protocol = builder.protocol;
        this.query = builder.query;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public HttpServer getServer() {
        return server;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getQuery() {
        return query;
    }

    public String getQuery(String key, boolean caseSensitive) {
        if (caseSensitive) {
            return getQueries().getOrDefault(key, "");
        }

        for (Map.Entry<String, String> entry : getQueries().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        return "";
    }

    public Map<String, String> getQueries() {
        if (queryCache == null) {
            queryCache = new HashMap<>();
            String[] entries = this.query.split("&");

            for (String entry : entries) {
                String[] pair = entry.split("=", 2);

                queryCache.put(pair[0], pair.length > 1 ? pair[1] : "");
            }
        }

        return queryCache;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key, boolean caseSensitive) {
        if (caseSensitive) {
            return headers.getOrDefault(key, "");
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        return "";
    }

    public byte[] getBody() {
        return body;
    }

    static class Builder {
        private HttpServer server;
        private SocketChannel channel;
        private HttpMethod method;
        private String path;
        private String protocol;
        private String query;
        private Map<String, String> headers;
        private byte[] body;

        Builder setServer(HttpServer server) {
            this.server = server;

            return this;
        }

        Builder setChannel(SocketChannel channel) {
            this.channel = channel;

            return this;
        }

        Builder setMethod(HttpMethod method) {
            this.method = method;

            return this;
        }

        Builder setPath(String path) {
            this.path = path;

            return this;
        }

        Builder setProtocol(String protocol) {
            this.protocol = protocol;

            return this;
        }

        Builder setQuery(String query) {
            this.query = query;

            return this;
        }

        Builder setHeaders(Map<String, String> headers) {
            this.headers = headers;

            return this;
        }

        Builder setBody(byte[] body) {
            this.body = body;

            return this;
        }

        HttpRequest build() {
            if (method == null) {
                this.method = HttpMethod.UNKNOWN;
            }

            if (path == null) {
                this.path = "";
            }

            if (protocol == null) {
                this.protocol = "";
            }

            if (query == null) {
                this.query = "";
            }

            if (headers == null) {
                this.headers = Collections.emptyMap();
            }

            if (body == null) {
                this.body = new byte[0];
            }

            return new HttpRequest(this);
        }
    }
}
