package io.github.winnpixie.webserver;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private final Request request;
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();
    private final Map<String, String> headers = new HashMap<>() {
        {
            put("Connection", "close");
        }
    };

    private int statusCode = 500;
    private String reasonPhrase = "Internal Server Error";

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

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @NotNull
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(@NotNull String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public void write() throws Exception {
        var os = request.getRequestThread().getSocketHandler().getOutputStream();
        if (os == null) throw new RuntimeException("No output stream to write to.");

        os.write("HTTP/1.0 %d %s\n".formatted(statusCode, reasonPhrase).getBytes(StandardCharsets.UTF_8));

        headers.forEach((key, value) -> {
            try {
                os.write("%s: %s\n".formatted(key, value).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        os.write("\n".getBytes(StandardCharsets.UTF_8));

        os.write(body.toByteArray());
    }
}
