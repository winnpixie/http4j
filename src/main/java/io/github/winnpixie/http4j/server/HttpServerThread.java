package io.github.winnpixie.http4j.server;

import io.github.winnpixie.http4j.server.endpoints.RequestHandler;
import io.github.winnpixie.http4j.server.incoming.Request;
import io.github.winnpixie.http4j.server.outgoing.Response;
import io.github.winnpixie.http4j.shared.HttpMethod;
import io.github.winnpixie.http4j.shared.HttpStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;

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
            server.getLogger().info(() -> String.format("http4j (server) started at %s:%d",
                    addr.getHostName(), addr.getPort()));

            while (server.isRunning()) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();

                    if (key.isAcceptable()) { // Server Poll
                        accept(srvChannel, selector);
                    } else if (key.isReadable() && key.isWritable()) { // Client Process
                        processClient(key);
                    }

                    keys.remove();
                }
            }
        } catch (IOException ioe) {
            server.getLogger().log(Level.WARNING, "Error opening channels", ioe);
        }
    }

    private void accept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void processClient(SelectionKey key) {
        try (SocketChannel channel = (SocketChannel) key.channel()) {
            Request request = read(channel);
            Response response = write(request);

            server.getLogger().info(() -> {
                try {
                    return String.format("addr=%s/x-fwd=%s path='%s' status-code=%d user-agent='%s'",
                            ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress(),
                            request.getHeader("X-Forwarded-For", false),
                            request.getPath(),
                            response.getStatus().getCode(),
                            request.getHeader("User-Agent", false));
                } catch (IOException ioe) {
                    server.getLogger().log(Level.WARNING, "Error retrieving client address", ioe);

                    return String.format("addr=err/x-fwd=%s path='%s' status-code=%d user-agent='%s'",
                            request.getHeader("X-Forwarded-For", false),
                            request.getPath(),
                            response.getStatus().getCode(),
                            request.getHeader("User-Agent", false));
                }
            });
        } catch (IOException ioe) {
            server.getLogger().log(Level.WARNING, "Error processing client", ioe);
        }
    }

    private Request read(SocketChannel channel) throws IOException {
        Request request = new Request(server, channel);
        request.read();

        return request;
    }

    private Response write(Request request) throws IOException {
        Response response = new Response();

        if (request.getHeader("Host", false).isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST);
        } else {
            RequestHandler handler = server.getRequestHandlers().getHandler(request.getPath());
            if (handler != null) response = handler.process(request);
        }

        response.write(request.getChannel(), !request.getMethod().equals(HttpMethod.HEAD));

        return response;
    }
}
