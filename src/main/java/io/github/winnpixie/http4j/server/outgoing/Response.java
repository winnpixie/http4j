package io.github.winnpixie.http4j.server.outgoing;

import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.utilities.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private final HttpStatus status;
    private final Map<String, String> headers;
    private final byte[] body;

    Response(Builder builder) {
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

    public void writeHead(SocketChannel channel) throws IOException {
        // Construct Head
        StringBuilder head = new StringBuilder("HTTP/1.1 ")
                .append(status.getCode()).append(' ')
                .append(status.getReasonPhrase())
                .append(Constants.END_OF_LINE);

        // Apply Headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            head.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append(Constants.END_OF_LINE);
        }
        head.append(Constants.END_OF_LINE);

        channel.write(StandardCharsets.UTF_8.encode(head.toString()));
    }

    public void writeBody(SocketChannel channel) throws IOException {
        byte[] content = this.body;

        // Generate simple error text if content body is empty
        if (content.length == 0 && status.getCode() / 100 != 2) {
            content = String.format("%d - %s",
                            status.getCode(),
                            status.getReasonPhrase())
                    .getBytes(StandardCharsets.UTF_8);
        }

        channel.write(ByteBuffer.wrap(content));
    }

    public static class Builder {
        private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
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

        public Response build() {
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            headers.put("Connection", "close");

            if (body == null) {
                this.body = new byte[0];
            }

            return new Response(this);
        }
    }
}
