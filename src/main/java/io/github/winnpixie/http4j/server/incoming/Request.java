package io.github.winnpixie.http4j.server.incoming;


import io.github.winnpixie.http4j.server.HttpServer;
import io.github.winnpixie.http4j.shared.HttpMethod;

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
    private Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    private Map<String, String> queryCache;

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

    public boolean read() throws IOException {
        ByteBuffer bufferIn = ByteBuffer.allocate(65536);
        // TODO: Use eventually?
        int bytesRead = channel.read(bufferIn);
        bufferIn.flip();

        if (!readHead(bufferIn)) throw new IOException("Malformed HTTP Head Line");
        readHeaders(bufferIn);
        readContent(bufferIn);

        return true;
    }

    private boolean readHead(ByteBuffer bufferIn) {
        String headLine = readLine(bufferIn);
        if (headLine == null) return false;

        String[] headTokens = headLine.split(" ");
        if (headTokens.length < 3) return false;
        this.method = HttpMethod.from(headTokens[0]);
        this.path = headTokens[1];

        int queryIdx = path.indexOf('?');
        if (queryIdx > 0) { // Query can not be the first character in path.
            this.query = path.substring(queryIdx + 1);
            this.path = path.substring(0, queryIdx);
        }

        this.protocol = headTokens[2];
        return true;
    }

    private void readHeaders(ByteBuffer bufferIn) {
        this.headers = new HashMap<>();

        String headerLine;
        while ((headerLine = readLine(bufferIn)) != null && headerLine.indexOf(':') > 0) {
            String[] header = headerLine.split(":", 2);
            headers.put(header[0], header[1].indexOf(' ') == 0 ? header[1].substring(1) : header[1]);
        }
    }

    private void readContent(ByteBuffer bufferIn) {
        // TODO: Add properly? reading request body, this seems to work *for now*
        String contentLengthHeader = getHeader("Content-Length", false);
        if (contentLengthHeader.isEmpty()) return;

        int contentLength = Integer.parseInt(contentLengthHeader);

        ByteBuffer buffer = ByteBuffer.allocate(contentLength);
        int chr;
        while (contentLength > 0 && (chr = readByte(bufferIn)) != -1) {
            buffer.put((byte) chr);
            contentLength--;
        }

        this.body = buffer.array();
    }

    private String readLine(ByteBuffer buf) {
        if (!buf.hasRemaining()) return null;

        StringBuilder builder = new StringBuilder();

        int ch;
        while ((ch = readByte(buf)) != -1) {
            if (ch == '\n') {
                if (!builder.isEmpty() && builder.charAt(builder.length() - 1) == '\r') {
                    builder.deleteCharAt(builder.length() - 1);
                }
                break;
            }

            builder.append((char) ch);
        }

        return builder.toString();
    }

    private int readByte(ByteBuffer buf) {
        return buf.hasRemaining() ? buf.get() : -1;
    }
}
