package io.github.winnpixie.http4j.server.outgoing;

import io.github.winnpixie.http4j.server.incoming.HttpRequest;
import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.HttpStatus;

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
            put("Server", "winnpixie/http4j (server)");
        }
    };

    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

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

    public void brewCoffee() {
        setStatus(HttpStatus.IM_A_TEAPOT);
    }

    public void write() throws IOException {
        OutputStream os = request.getRequestThread().getSocket().getOutputStream();
        if (os == null) throw new RuntimeException("No output stream to write to.");

        if (request.getProtocol().startsWith("HTCPCP/")) brewCoffee();

        os.write("HTTP/1.1 %d %s\n".formatted(status.getCode(), status.getReasonPhrase()).getBytes(StandardCharsets.UTF_8));

        headers.forEach((key, value) -> {
            try {
                os.write("%s: %s\n".formatted(key, value).getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        os.write('\n');

        if (body.size() == 0 && status.getCode() / 100 != 2) body.write("%d %s".formatted(status.getCode(),
                status.getReasonPhrase()).getBytes(StandardCharsets.UTF_8));

        // HEAD = only headers get sent
        if (!request.getMethod().equals(HttpMethod.HEAD)) os.write(body.toByteArray());
    }
}
