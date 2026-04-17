package io.github.winnpixie.http4j.server.incoming;

import io.github.winnpixie.http4j.server.HttpServer;
import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.throwables.HttpException;
import io.github.winnpixie.http4j.shared.utilities.IOHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestProvider {
    private HttpRequestProvider() {
    }

    public static HttpRequest create(HttpServer server, SocketChannel channel) throws HttpException {
        ByteBuffer buffer = ByteBuffer.allocate(8192); // 8K buffer
        int len;

        try {
            len = channel.read(buffer);
        } catch (IOException ioe) {
            throw new HttpException("Error reading request", ioe);
        }

        if (len < 1) {
            throw new HttpException("No data received");
        }

        buffer.flip();

        HttpRequest.Builder builder = new HttpRequest.Builder()
                .setServer(server)
                .setChannel(channel);

        readHead(builder, buffer);

        Map<String, String> headers = readHeaders(builder, buffer);
        if (headers.isEmpty()) {
            return builder.build(); // We couldn't read all the headers, do not process the content body
        }

        builder.setBody(readContent(headers, server, channel, buffer));

        return builder.build();
    }


    private static void readHead(HttpRequest.Builder builder, ByteBuffer buffer) throws HttpException {
        String head = IOHelper.readLine(buffer);
        if (head == null) {
            throw new HttpException("Missing HTTP Head");
        }

        String[] tokens = head.split(" ");
        if (tokens.length < 3) {
            throw new HttpException(String.format("Missing Tokens for HTTP Head (%d < 3)",
                    tokens.length));
        }

        String path = tokens[1];
        builder.setMethod(HttpMethod.from(tokens[0]))
                .setPath(path)
                .setProtocol(tokens[2]);

        // Is this the proper way to handle the path+query portion of the URI?
        int queryIdx = path.indexOf('?');
        if (queryIdx > -1) {
            builder.setQuery(path.substring(queryIdx + 1))
                    .setPath(path.substring(0, queryIdx));
        }
    }

    // FIXME: This needs to be implemented better
    private static Map<String, String> readHeaders(HttpRequest.Builder builder, ByteBuffer buffer) {
        Map<String, String> headers = new HashMap<>();
        builder.setHeaders(headers);

        String line;
        while ((line = IOHelper.readLine(buffer)) != null) {
            if (line.isEmpty()) {
                return headers;
            }

            String[] header = line.split(":", 2);
            if (header.length < 2) {
                break;
            }

            String value = header[1];
            if (value.indexOf(' ') == 0) {
                value = value.substring(1);
            }

            headers.put(header[0], value);
        }

        return Collections.emptyMap();
    }

    private static byte[] readContent(Map<String, String> headers, HttpServer server, SocketChannel channel, ByteBuffer buffer) throws HttpException {
        int contentLength = getContentLength(headers, server);
        if (contentLength < 1) {
            return new byte[0];
        }

        int remaining = contentLength - buffer.remaining();
        ByteBuffer contentBuffer = ByteBuffer.allocate(contentLength);
        contentBuffer.put(buffer);

        while (remaining > 0) {
            try {
                remaining -= channel.read(contentBuffer);
            } catch (IOException ioe) {
                throw new HttpException("Error content body", ioe);
            }
        }

        return contentBuffer.array();
    }

    private static int getContentLength(Map<String, String> headers, HttpServer server) throws HttpException {
        String contentLengthString = headers.getOrDefault("Content-Length", "");
        if (contentLengthString.isEmpty()) {
            return 0;
        }

        int contentLength;
        try {
            contentLength = Integer.parseInt(contentLengthString);
        } catch (NumberFormatException nfe) {
            throw new HttpException("Content-Length is not a number", nfe);
        }

        if (contentLength < 0) {
            throw new HttpException(String.format("Content-Length cannot be negative (%d)",
                    contentLength));
        }

        if (contentLength > server.getContentLengthLimit()) {
            throw new HttpException(String.format("Content-Length exceeds limit (%d > %d)",
                    contentLength, server.getContentLengthLimit()));
        }

        return contentLength;
    }
}
