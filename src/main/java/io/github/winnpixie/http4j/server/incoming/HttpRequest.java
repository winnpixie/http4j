package io.github.winnpixie.http4j.server.incoming;


import io.github.winnpixie.http4j.shared.HttpMethod;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final HttpRequestThread requestThread;

    private HttpMethod method;
    private String path;
    private String query;
    private String protocol;
    private Map<String, String> headers;
    private byte[] body;

    private Map<String, String> queryCache;

    public HttpRequest(HttpRequestThread requestThread) {
        this.requestThread = requestThread;

        this.method = HttpMethod.UNKNOWN;
        this.path = "";
        this.query = "";
        this.protocol = "";
        this.headers = new HashMap<>();
        this.body = new byte[0];
    }

    public HttpRequestThread getRequestThread() {
        return requestThread;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getQuery(String key, boolean caseSensitive) {
        for (Map.Entry<String, String> query : getQueries().entrySet()) {
            if (query.getKey().equals(key)) return query.getValue();
            if (!caseSensitive && query.getKey().equalsIgnoreCase(key)) return query.getValue();
        }

        return "";
    }

    public Map<String, String> getQueries() {
        if (queryCache == null) {
            queryCache = new HashMap<>();
            String[] entries = this.query.split("&");

            for (String entry : entries) {
                String[] pair = entry.split("=", 2);
                queryCache.put(pair[0], pair.length > 1 ? pair[1] : "");
            }
        }

        return queryCache;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name, boolean exact) {
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

    public void read() throws IOException {
        InputStream is = requestThread.getSocket().getInputStream();
        if (is == null) throw new RuntimeException("Unable to retrieve input stream.");

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String[] httpHeader = reader.readLine().split(" ");
        this.method = HttpMethod.from(httpHeader[0]);
        this.path = httpHeader[1];

        int queryIdx = path.indexOf('?');
        if (queryIdx > 0) { // Query can not be the first character in path.
            this.query = path.substring(queryIdx + 1);
            this.path = path.substring(0, queryIdx);
        }

        this.protocol = httpHeader[2];

        this.headers = new HashMap<>();
        String headerLine;
        while ((headerLine = reader.readLine()) != null && headerLine.indexOf(':') > 0) {
            String[] header = headerLine.split(":", 2);
            headers.put(header[0], header[1].indexOf(' ') == 0 ? header[1].substring(1) : header[1]);
        }

        // TODO: Add properly? reading request body, this seems to work *for now*
        String reportedContentLength = getHeader("Content-Length", false);
        if (reportedContentLength.isEmpty()) return;

        int contentLength = Integer.parseInt(reportedContentLength);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int chr;
            while (contentLength > 0 && (chr = reader.read()) != -1) {
                baos.write(chr);

                contentLength--;
            }

            this.body = baos.toByteArray();
        }
    }
}
