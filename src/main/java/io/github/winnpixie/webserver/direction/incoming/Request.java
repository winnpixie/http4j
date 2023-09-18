package io.github.winnpixie.webserver.direction.incoming;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
    public Map<String, String> getQueries() {
        Map<String, String> queries = new HashMap<>();
        String[] entries = this.query.split("&");

        for (String entry : entries) {
            String[] pair = entry.split("=", 2);
            queries.put(pair[0], pair.length > 1 ? pair[1] : "");
        }

        return queries;
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

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(name)) continue;

            return entry.getValue();
        }

        return "";
    }

    public byte[] getBody() {
        return body;
    }

    public void read() throws Exception {
        InputStream is = requestThread.getSocketHandler().getInputStream();
        if (is == null) throw new RuntimeException("Unable to retrieve input stream.");

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String[] httpHeader = reader.readLine().split(" ");
        this.method = httpHeader[0];
        this.path = httpHeader[1];

        int queryIdx = path.indexOf('?');
        if (queryIdx > 0) { // Query can not be the first character in path.
            this.query = path.substring(queryIdx + 1);
            this.path = path.substring(0, queryIdx);
        }

        this.protocol = httpHeader[2];

        this.headers = new HashMap<>();
        String headerLine = "";
        while ((headerLine = reader.readLine()) != null && headerLine.indexOf(':') > 0) {
            String[] header = headerLine.split(":", 2);
            headers.put(header[0], header[1].indexOf(' ') == 0 ? header[1].substring(1) : header[1]);
        }

        // TODO: Add properly? reading request body, this seems to work *for now*
        String reportedContentLength = getHeader("Content-Length", false);
        if (reportedContentLength.isEmpty()) return;

        int contentLength = Integer.parseInt(reportedContentLength);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int chr = -1;
            while (contentLength > 0 && (chr = reader.read()) != -1) {
                baos.write(chr);

                contentLength--;
            }

            this.body = baos.toByteArray();
        }
    }
}
