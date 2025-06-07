package io.github.foss4j.http4j.server.outgoing;

import io.github.foss4j.http4j.shared.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public Response() {
        headers.put("Connection", "close");
        headers.put("Server", "foss4j/http4j (server)");
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

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
