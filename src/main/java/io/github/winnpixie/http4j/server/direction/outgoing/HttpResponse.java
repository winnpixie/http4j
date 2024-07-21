package io.github.winnpixie.http4j.server.direction.outgoing;

import io.github.winnpixie.http4j.server.direction.incoming.HttpRequest;
import io.github.winnpixie.http4j.server.direction.incoming.HttpMethod;
import io.github.winnpixie.http4j.shared.HttpResponseStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final HttpRequest request;
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();
    private final Map<String, String> headers = new HashMap<>() {
        {
            put("Connection", "close");
            put("Server", "winnpixie/http4j");
        }
    };

    private HttpResponseStatus status = HttpResponseStatus.INTERNAL_SERVER_ERROR;

    public HttpResponse(HttpRequest request) {
        this.request = request;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public ByteArrayOutputStream getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }

    public void setRedirect(HttpResponseStatus status, String destination) {
        setStatus(status);
        setHeader("Location", destination);
    }

    public void setPermanentRedirect(String destination) {
        setRedirect(HttpResponseStatus.MOVED_PERMANENTLY, destination);
    }

    public void setTemporaryRedirect(String destination) {
        setRedirect(HttpResponseStatus.MOVED_TEMPORARILY, destination);
    }

    public void brewCoffee() {
        setStatus(HttpResponseStatus.IM_A_TEAPOT);
    }

    public void write() throws Exception {
        OutputStream os = request.getRequestThread().getSocketHandler().getOutputStream();
        if (os == null) throw new RuntimeException("No output stream to write to.");

        if (request.getProtocol().startsWith("HTCPCP/")) brewCoffee();

        os.write("HTTP/1.0 %d %s\n".formatted(status.getCode(), status.getReasonPhrase()).getBytes(StandardCharsets.UTF_8));

        headers.forEach((key, value) -> {
            try {
                os.write("%s: %s\n".formatted(key, value).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        byte[] body = this.body.toByteArray();
        os.write("Content-Length: %d\n\n".formatted(body.length).getBytes(StandardCharsets.UTF_8));
        if (!request.getMethod().equals(HttpMethod.HEAD)) os.write(body);
    }
}
