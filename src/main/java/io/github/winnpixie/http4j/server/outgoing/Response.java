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

    public void write(SocketChannel channel, boolean writeBody) throws IOException {
        StringBuilder head = new StringBuilder("HTTP/1.1 ")
                .append(status.getCode()).append(' ')
                .append(status.getReasonPhrase())
                .append(Constants.END_OF_LINE);

        headers.forEach((key, value) -> head.append(key)
                .append(": ")
                .append(value)
                .append(Constants.END_OF_LINE));
        head.append(Constants.END_OF_LINE);

        channel.write(StandardCharsets.UTF_8.encode(head.toString()));

        if (!writeBody) {
            return;
        }

        if (body.length == 0 && status.getCode() / 100 != 2) {
            channel.write(ByteBuffer.wrap(String.format("%d %s",
                            status.getCode(),
                            status.getReasonPhrase())
                    .getBytes(StandardCharsets.UTF_8)));
        } else {
            channel.write(ByteBuffer.wrap(body));
        }
    }

    public static class Builder {
        private HttpStatus status;
        private Map<String, String> headers;
        private byte[] body;

        public Builder() {
            this.status = HttpStatus.INTERNAL_SERVER_ERROR;
            this.headers = new HashMap<>();
        }

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
            return setRedirect(HttpStatus.MOVED_PERMANENTLY, destination);
        }

        public Builder setTemporaryRedirect(String destination) {
            return setRedirect(HttpStatus.MOVED_TEMPORARILY, destination);
        }

        public Response build() {
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            headers.put("Connection", "close");
            headers.put("Server", Constants.SERVER_ID);

            if (body == null) {
                this.body = new byte[0];
            }

            return new Response(this);
        }
    }
}
