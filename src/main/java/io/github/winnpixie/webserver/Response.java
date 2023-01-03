package io.github.winnpixie.webserver;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class Response {
    private final Request request;
    private final ByteArrayOutputStream payload;

    private int responseCode;
    private String responseInfo;

    public Response(@NotNull Request request) {
        this.request = request;

        this.payload = new ByteArrayOutputStream();
        this.responseInfo = "";
    }

    public int getResponseCode() {
        return responseCode;
    }

    @NotNull
    public String getResponseInfo() {
        return responseInfo;
    }

    public void prepare() throws Exception {
        var file = new File(request.getRequestThread().getServer().getRootDirectory(), request.getPath());
        if (file.isDirectory()) file = new File(file, "index.html");

        try (var bodyPayload = new ByteArrayOutputStream()) {
            if (!request.getHeaders().containsKey("Host")) {
                responseCode = 400;
                responseInfo = "Bad Request";
            } else if (!file.getCanonicalPath().startsWith(request.getRequestThread().getServer().getRootDirectory().getCanonicalPath())
                    || !file.exists()) {
                if (file.exists()) {
                    System.out.println("Prevented read from file outside of server root directory");
                }
                responseCode = 404;
                responseInfo = "Not Found";
            } else {
                responseCode = 200;
                responseInfo = "OK";

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    int ch;
                    while ((ch = reader.read()) != -1) {
                        bodyPayload.write(ch);
                    }
                }
            }

            bodyPayload.flush();

            payload.writeBytes(String.format("HTTP/1.0 %d %s\n", responseCode, responseInfo).getBytes(StandardCharsets.UTF_8));
            payload.writeBytes("Connection: close\n".getBytes(StandardCharsets.UTF_8));
            payload.writeBytes("\n".getBytes(StandardCharsets.UTF_8));
            payload.write(bodyPayload.toByteArray());
            payload.flush();
        }
    }

    public void write() throws Exception {
        var os = request.getRequestThread().getSocketHandler().getOutputStream();
        if (os == null) throw new RuntimeException("No output stream to write to!");

        payload.flush();
        os.write(payload.toByteArray());
    }
}
