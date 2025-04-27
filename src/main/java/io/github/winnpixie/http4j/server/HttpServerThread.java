package io.github.winnpixie.http4j.server;

import io.github.winnpixie.http4j.server.endpoints.RequestHandler;
import io.github.winnpixie.http4j.server.incoming.Request;
import io.github.winnpixie.http4j.server.outgoing.Response;
import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.HttpStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class HttpServerThread extends Thread {
    private final HttpServer server;

    public HttpServerThread(HttpServer server) {
        super("http4j_server");

        this.server = server;
    }

    @Override
    public void run() {
        try (Selector selector = Selector.open();
             ServerSocketChannel srvChannel = ServerSocketChannel.open()) {
            srvChannel.configureBlocking(false);
            srvChannel.bind(new InetSocketAddress(server.getPort()));
            srvChannel.register(selector, SelectionKey.OP_ACCEPT);

            InetSocketAddress addr = (InetSocketAddress) srvChannel.getLocalAddress();
            server.getLogger().info("http4j (server) started at %s:%d"
                    .formatted(addr.getHostName(), addr.getPort()));

            while (server.isRunning()) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();

                    if (key.isAcceptable()) { // Server Poll
                        accept(srvChannel, selector);
                    } else if (key.isReadable() && key.isWritable()) { // Client Process
                        try (SocketChannel channel = (SocketChannel) key.channel()) {
                            Request request = read(channel);
                            if (request != null) {
                                Response response = write(request);

                                server.getLogger().info("addr=%s/x-fwd=%s path='%s' status-code=%d user-agent='%s'"
                                        .formatted(((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress(),
                                                request.getHeader("X-Forwarded-For", false),
                                                request.getPath(),
                                                response.getStatus().getCode(),
                                                request.getHeader("User-Agent", false)));
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                    keys.remove();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void accept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private Request read(SocketChannel channel) throws IOException {
        Request request = new Request(server, channel);
        if (!request.read()) return null;

        return request;
    }

    private Response write(Request request) throws IOException {
        Response response = new Response();

        if (request.getHeader("Host", false).isEmpty() || request.getPath().indexOf('/') > 0) {
            response.setStatus(HttpStatus.BAD_REQUEST);
        } else {
            RequestHandler handler = server.getRequestHandlers().getHandler(request.getPath());
            if (handler != null) response = handler.process(request);
        }

        StringBuilder head = new StringBuilder("HTTP/1.1 %d %s\n".formatted(response.getStatus().getCode(),
                response.getStatus().getReasonPhrase()));

        response.getHeaders().forEach((headerKey, value) -> head.append("%s: %s\n".formatted(headerKey, value)));
        head.append('\n');

        request.getChannel().write(StandardCharsets.UTF_8.encode(head.toString()));

        if (response.getBody().length == 0 && response.getStatus().getCode() / 100 != 2)
            response.setBody("%d %s".formatted(response.getStatus().getCode(),
                    response.getStatus().getReasonPhrase()).getBytes(StandardCharsets.UTF_8));

        // HEAD = only headers get sent
        if (!request.getMethod().equals(HttpMethod.HEAD))
            request.getChannel().write(ByteBuffer.wrap(response.getBody()));

        return response;
    }
}
