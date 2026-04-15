package io.github.winnpixie.http4j.client;

import io.github.winnpixie.http4j.shared.HttpStatus;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Response {
    private final Request request;
    private final HttpStatus status;
    private final Map<String, List<String>> headers;
    private final byte[] body;

    Response(Builder builder) {
        this.request = builder.request;
        this.status = builder.status;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public Request getRequest() {
        return request;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<String> getHeader(String key, boolean caseSensitive) {
        if (caseSensitive) {
            return headers.getOrDefault(key, Collections.emptyList());
        }

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        return Collections.emptyList();
    }

    public byte[] getBody() {
        return body;
    }

    public <T> T getBodyAs(Function<byte[], T> mutator) {
        return mutator.apply(body);
    }

    public String getBodyAsString(Charset charset) {
        return new String(body, charset);
    }

    public String getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }

    static class Builder {
        private Request request;
        private HttpStatus status;
        private Map<String, List<String>> headers;
        private byte[] body;

        Builder setRequest(Request request) {
            this.request = request;

            return this;
        }

        Builder setStatus(HttpStatus status) {
            this.status = status;
            return this;
        }

        Builder setHeaders(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        Builder setBody(byte[] body) {
            this.body = body;
            return this;
        }

        Response build() {
            if (status == null) {
                this.status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            if (headers == null) {
                this.headers = new HashMap<>();
            }

            if (body == null) {
                this.body = new byte[0];
            }

            return new Response(this);
        }
    }
}