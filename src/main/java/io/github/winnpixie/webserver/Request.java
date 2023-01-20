package io.github.winnpixie.webserver;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final RequestThread requestThread;

    private String method;
    private String path;
    private String query;
    private String protocol;
    private Map<String, String> headers;
    private byte[] body;

    public Request(@NotNull RequestThread requestThread) {
        this.requestThread = requestThread;

        this.method = "";
        this.path = "";
        this.query = "";
        this.protocol = "";
        this.headers = new HashMap<>();
        this.body = new byte[0];
    }

    @NotNull
    public RequestThread getRequestThread() {
        return requestThread;
    }

    @NotNull
    public String getMethod() {
        return method;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String getQuery() {
        return query;
    }

    @NotNull
    public String getQuery(@NotNull String key, boolean exact) {
        String[] entries = this.query.split("&");

        for (String entry : entries) {
            String[] pair = entry.split("=", 2);

            if (!pair[0].equals(key) && exact) continue;
            if (!pair[0].equalsIgnoreCase(key)) continue;

            return pair.length > 1 ? pair[1] : "";
        }

        return "";
    }

    @NotNull
    public String getProtocol() {
        return protocol;
    }

    @NotNull
    public Map<String, String> getHeaders() {
        return headers;
    }

    @NotNull
    public String getHeader(@NotNull String name, boolean exact) {
        if (exact) return headers.getOrDefault(name, "");

        for (var entry : headers.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(name)) continue;

            return entry.getValue();
        }

        return "";
    }

    public byte[] getBody() {
        return body;
    }

    public void read() throws Exception {
        var is = requestThread.getSocketHandler().getInputStream();
        if (is == null) throw new RuntimeException("Unable to retrieve input stream!");

        var reader = new BufferedReader(new InputStreamReader(is));
        var httpHeader = reader.readLine().split(" ");
        this.method = httpHeader[0];
        this.path = httpHeader[1];

        int queryIdx = path.indexOf('?');
        if (queryIdx > 0) { // Query can not be the first character in path.
            if (queryIdx != path.length() - 1) {
                this.query = path.substring(queryIdx + 1);
            }

            this.path = path.substring(0, queryIdx);
        }

        this.protocol = httpHeader[2];

        this.headers = new HashMap<>();
        String headerLine;
        while ((headerLine = reader.readLine()) != null && headerLine.contains(":")) {
            var header = headerLine.split(":", 2);
            headers.put(header[0], header[1].startsWith(" ") ? header[1].substring(1) : header[1]);
        }

        // TODO: Add properly? reading request body, this seems to work *for now*
        var contentLengthHeader = getHeader("Content-Length", false);
        if (contentLengthHeader.isEmpty()) return;

        var contentLength = Integer.parseInt(contentLengthHeader);

        try (var baos = new ByteArrayOutputStream()) {
            int ch;
            while (contentLength > 0 && (ch = reader.read()) != -1) {
                baos.write(ch);

                contentLength--;
            }

            this.body = baos.toByteArray();
        }
    }
}
