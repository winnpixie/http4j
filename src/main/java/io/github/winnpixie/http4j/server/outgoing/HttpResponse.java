package io.github.winnpixie.http4j.server.outgoing;

import io.github.winnpixie.http4j.shared.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final HttpStatus status;
    private final Map<String, String> headers;
    private final byte[] body;

    private HttpResponse(Builder builder) {
        this.status = builder.status;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public static class Builder {
        private HttpStatus status;
        private Map<String, String> headers = new HashMap<>();
        private byte[] body;

        public Builder setStatus(HttpStatus status) {
            this.status = status;

            return this;
        }

        public Builder setHeaders(Map<String, String> headers) {
            if (headers == null) {
                headers = new HashMap<>();
            }

            this.headers = headers;

            return this;
        }

        public Builder setHeader(String key, String value) {
            headers.put(key, value);

            return this;
        }

        public Builder setBody(byte[] body) {
            this.body = body;

            return this;
        }

        public Builder setRedirect(HttpStatus status, String destination) {
            setStatus(status);
            setHeader("Location", destination);

            return this;
        }

        public Builder setPermanentRedirect(String destination) {
            return setRedirect(HttpStatus.PERMANENT_REDIRECT, destination);
        }

        public Builder setTemporaryRedirect(String destination) {
            return setRedirect(HttpStatus.TEMPORARY_REDIRECT, destination);
        }

        public HttpResponse build() {
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            headers.put("Connection", "close");

            if (body == null) {
                this.body = new byte[0];
            }

            return new HttpResponse(this);
        }
    }
}
