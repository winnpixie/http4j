package io.github.winnpixie.http4j.server.endpoints.impl;

import io.github.winnpixie.http4j.server.endpoints.HttpEndpoint;
import io.github.winnpixie.http4j.server.incoming.HttpRequest;
import io.github.winnpixie.http4j.server.outgoing.HttpResponse;
import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.utilities.FileHelper;
import io.github.winnpixie.http4j.shared.utilities.IOHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHttpEndpoint extends HttpEndpoint {
    private Path root;

    public FileHttpEndpoint() {
        super("/");
    }

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        this.root = root;
    }

    @Override
    public void handle(HttpResponse response) {
        HttpRequest request = response.getRequest();
        String pathStr = request.getPath().substring(1);
        Path path = root.resolve(pathStr);

        // A file can not be a directory.
        boolean isDir = Files.isDirectory(path);
        if (!isDir && pathStr.endsWith("/")) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        // Attempt to locate an index.html file if requested resource is a directory.
        if (isDir) path = path.resolve("index.html");
        if (Files.notExists(path)) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        // Attempt to prevent escaping the root directory.
        Path normalizedPath = path.toAbsolutePath().normalize();
        Path normalizedRoot = root.toAbsolutePath().normalize();
        if (!normalizedPath.startsWith(normalizedRoot)) {
            request.getRequestThread().getServer().getLogger()
                    .warning("Prevented read from path outside of root!");

            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        try (ByteArrayOutputStream bodyStream = response.getBody();
             ByteArrayInputStream byteStream = new ByteArrayInputStream(Files.readAllBytes(path))) {
            IOHelper.transfer(byteStream, bodyStream);
            response.setHeader("Content-Length", Integer.toString(bodyStream.size()));

            byteStream.reset();
            String mime = FileHelper.guessMime(byteStream);
            if (mime.equals("application/octet-stream")) mime = FileHelper.guessMime(path.toString());
            response.setHeader("Content-Type", mime);

            response.setStatus(HttpStatus.OK);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
