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
        for (Map.Entry<String, String> entry : getQueries().entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }

            if (!caseSensitive && entry.getKey().equalsIgnoreCase(key)) {
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

    public String getHeader(String name, boolean exact) {
        if (exact) return headers.getOrDefault(name, "");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }

        return "";
    }

    public byte[] getBody() {
        return body;
    }

    public void read() throws IOException {
        ByteBuffer bufferIn = ByteBuffer.allocate(65536); // 64K buffer allocation
        int readLength = channel.read(bufferIn);
        if (readLength < 1) {
            throw new IOException("No input");
        }

        bufferIn.flip();

        if (!readHead(bufferIn)) {
            throw new IOException("Malformed HTTP Head Line");
        }

        readHeaders(bufferIn);
        readContent(bufferIn);
    }

    private boolean readHead(ByteBuffer bufferIn) {
        String headLine = readLine(bufferIn);
        if (headLine == null) {
            return false;
        }

        String[] headTokens = headLine.split(" ");
        if (headTokens.length < 3) {
            return false;
        }

        this.method = HttpMethod.from(headTokens[0]);
        this.path = headTokens[1];
        this.protocol = headTokens[2];

        int queryTokenIndex = path.indexOf('?');
        if (queryTokenIndex > 0) { // Query can not be the first character in path.
            this.query = path.substring(queryTokenIndex + 1);
            this.path = path.substring(0, queryTokenIndex);
        }

        return true;
    }

    private void readHeaders(ByteBuffer bufferIn) {
        this.headers = new HashMap<>();

        String line;
        while ((line = readLine(bufferIn)) != null) {
            String[] header = line.split(":", 2);
            if (header.length < 2) {
                break;
            }

            headers.put(header[0], header[1].indexOf(' ') == 0 ? header[1].substring(1) : header[1]);
        }
    }

    // TODO: Look into a "proper" way of reading request content.
    private void readContent(ByteBuffer bufferIn) {
        String contentLength = getHeader("Content-Length", false);
        if (contentLength.isEmpty()) return;

        int expectedLength = Integer.parseInt(contentLength);

        ByteBuffer contentBuffer = ByteBuffer.allocate(expectedLength);
        byte b;
        while (expectedLength > 0 && (b = readByte(bufferIn)) != -1) {
            expectedLength--;
            contentBuffer.put(b);
        }

        this.body = (byte[]) contentBuffer.flip().array();
    }

    private String readLine(ByteBuffer bufferIn) {
        if (!bufferIn.hasRemaining()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        byte ch;
        while ((ch = readByte(bufferIn)) != -1) {
            boolean end = ch == '\r' || ch == '\n';
            if (ch == '\r') {
                ch = readByte(bufferIn); // consume next byte
                end = ch == -1 || ch == '\n';
            }

            if (end) {
                break;
            }

            builder.append((char) ch);
        }

        return builder.toString();
    }

    private byte readByte(ByteBuffer bufferIn) {
        return bufferIn.hasRemaining() ? bufferIn.get() : -1;
    }
}
