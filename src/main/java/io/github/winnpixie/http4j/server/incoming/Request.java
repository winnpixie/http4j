package io.github.winnpixie.http4j.server.incoming;


import io.github.winnpixie.http4j.server.HttpServer;
import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.utilities.IOHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final HttpServer server;
    private final SocketChannel channel;

    private HttpMethod method = HttpMethod.UNKNOWN;
    private String path = "";
    private String query = "";
    private String protocol = "";
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    private Map<String, String> queryCache; // lazily loaded

    public Request(HttpServer server, SocketChannel channel) {
        this.server = server;
        this.channel = channel;
    }

    public HttpServer getServer() {
        return server;
    }

    public SocketChannel getChannel() {
        return channel;
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
        if (caseSensitive) {
            return getQueries().getOrDefault(key, "");
        }

        for (Map.Entry<String, String> entry : getQueries().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
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

    public String getHeader(String key, boolean caseSensitive) {
        if (caseSensitive) {
            return headers.getOrDefault(key, "");
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        return "";
    }

    public byte[] getBody() {
        return body;
    }

    public void read() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024); // 1K buffer

        int length = channel.read(buffer);
        if (length < 1) {
            throw new IOException("No data received");
        }

        buffer.flip();

        readHead(buffer);
        readHeaders(buffer);

        // FIXME: Since the buffer is so small, the result may not represent the complete content
        readContent(buffer);
    }

    private void readHead(ByteBuffer buffer) throws IOException {
        String head = IOHelper.readLine(buffer);
        if (head == null) {
            throw new IOException("Missing HTTP Head");
        }

        String[] tokens = head.split(" ");
        if (tokens.length < 3) {
            throw new IOException("Missing Tokens for HTTP Head");
        }

        this.method = HttpMethod.from(tokens[0]);
        this.path = tokens[1];
        this.protocol = tokens[2];

        // Is this the proper way to handle the path+query portion of the URI?
        int queryIdx = path.indexOf('?');
        if (queryIdx > -1) {
            this.query = path.substring(queryIdx + 1);
            this.path = path.substring(0, queryIdx);
        }
    }

    private void readHeaders(ByteBuffer buffer) {
        String line;
        while ((line = IOHelper.readLine(buffer)) != null) {
            String[] header = line.split(":", 2);
            if (header.length < 2) {
                break;
            }

            String key = header[0];
            String value = header[1];
            if (value.indexOf(' ') == 0) {
                value = value.substring(1);
            }

            headers.put(key, value);
        }
    }

    private void readContent(ByteBuffer buffer) throws IOException {
        String contentLengthString = getHeader("Content-Length", true);
        if (contentLengthString.isEmpty()) {
            return;
        }

        int contentLength;
        try {
            contentLength = Integer.parseInt(contentLengthString);
        } catch (NumberFormatException nfe) {
            throw new IOException("Content-Length is not a number", nfe);
        }

        if (contentLength < 0) {
            throw new IOException("Content-Length cannot be negative");
        }

        if (contentLength > server.getContentLengthLimit()) {
            throw new IOException("Content-Length exceeds limit");
        }

        // TODO: Can we avoid array copy altogether, without over-sizing?
        byte[] content = new byte[contentLength];
        int read = 0;
        while (read < contentLength && buffer.hasRemaining()) {
            content[read++] = buffer.get();
        }

        this.body = new byte[read];
        System.arraycopy(content, 0, body, 0, read);
    }
}
