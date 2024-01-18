package io.github.winnpixie.httpsrv.direction.outgoing;

import io.github.winnpixie.httpsrv.direction.incoming.Request;
import io.github.winnpixie.httpsrv.direction.incoming.RequestMethod;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private final Request request;
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();
    private final Map<String, String> headers = new HashMap<>() {
        {
            put("Connection", "close");
            put("Server", "http-srv/0.3");
        }
    };

    private ResponseStatus status = ResponseStatus.INTERNAL_SERVER_ERROR;

    public Response(@NotNull Request request) {
        this.request = request;
    }

    @NotNull
    public Request getRequest() {
        return request;
    }

    @NotNull
    public ByteArrayOutputStream getBody() {
        return body;
    }

    @NotNull
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeader(@NotNull String key, @NotNull String value) {
        headers.put(key, value);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public void setRedirect(ResponseStatus status, String destination) {
        setStatus(status);
        setHeader("Location", destination);
    }

    public void setPermanentRedirect(String destination) {
        setRedirect(ResponseStatus.MOVED_PERMANENTLY, destination);
    }

    public void setTemporaryRedirect(String destination) {
        setRedirect(ResponseStatus.MOVED_TEMPORARILY, destination);
    }

    public void brewCoffee() {
        setStatus(ResponseStatus.IM_A_TEAPOT);
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
        if (!request.getMethod().equals(RequestMethod.HEAD)) os.write(body);
    }
}
