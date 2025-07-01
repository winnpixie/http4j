package io.github.winnpixie.http4j.server.outgoing;

import io.github.winnpixie.http4j.shared.HttpStatus;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public Response() {
        headers.put("Connection", "close");
        headers.put("Server", "winnpixie/http4j (server)");
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setRedirect(HttpStatus status, String destination) {
        setStatus(status);
        setHeader("Location", destination);
    }

    public void setPermanentRedirect(String destination) {
        setRedirect(HttpStatus.MOVED_PERMANENTLY, destination);
    }

    public void setTemporaryRedirect(String destination) {
        setRedirect(HttpStatus.MOVED_TEMPORARILY, destination);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void write(SocketChannel channel, boolean writeBody) throws IOException {
        StringBuilder head = new StringBuilder("HTTP/1.1")
                .append(status.getCode())
                .append(status.getReasonPhrase())
                .append('\n');

        headers.forEach((key, value) -> head.append(key)
                .append(": ")
                .append(value)
                .append('\n'));
        head.append('\n');

        channel.write(StandardCharsets.UTF_8.encode(head.toString()));

        if (!writeBody) return;

        if (body.length == 0 && status.getCode() / 100 != 2)
            setBody(String.format("%d %s",
                            status.getCode(),
                            status.getReasonPhrase())
                    .getBytes(StandardCharsets.UTF_8));

        channel.write(ByteBuffer.wrap(body));
    }
}
