package io.github.winnpixie.webserver;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private final Request request;
    private final ByteArrayOutputStream body;
    private final Map<String, String> headers;

    private int code;
    private String codeInfo;

    public Response(@NotNull Request request) {
        this.request = request;

        this.headers = new HashMap<>();
        this.headers.put("Connection", "close");

        this.body = new ByteArrayOutputStream();
        this.code = 500;
        this.codeInfo = "Internal Server Error";
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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @NotNull
    public String getCodeInfo() {
        return codeInfo;
    }

    public void setCodeInfo(@NotNull String codeInfo) {
        this.codeInfo = codeInfo;
    }

    public void write() throws Exception {
        var os = request.getRequestThread().getSocketHandler().getOutputStream();
        if (os == null) throw new RuntimeException("No output stream to write to!");

        os.write("HTTP/1.0 %d %s\n".formatted(code, codeInfo).getBytes(StandardCharsets.UTF_8));

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
