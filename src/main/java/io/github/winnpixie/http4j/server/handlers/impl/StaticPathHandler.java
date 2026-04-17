package io.github.winnpixie.http4j.server.handlers.impl;

import io.github.winnpixie.http4j.server.handlers.PathHandler;
import io.github.winnpixie.http4j.server.incoming.HttpRequest;
import io.github.winnpixie.http4j.server.outgoing.HttpResponse;
import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.throwables.HttpException;
import io.github.winnpixie.http4j.shared.utilities.FileHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;

public class StaticPathHandler extends PathHandler {
    private Path root;

    public StaticPathHandler() {
        super("/");
    }

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        try {
            // Cache as fully qualified path (to avoid re-resolving each security check)
            this.root = root.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException ioe) {
            // Ignore throw and set to provided
            // SECURITY: The consequence of this behavior may result in accidental 5XX errors
            this.root = root;
        }
    }

    @Override
    public HttpResponse process(HttpRequest request) throws HttpException {
        String pathString = request.getPath().substring(1);
        Path path = root.resolve(pathString);

        // Locate "index.html" if path is (or implied to be [ ends-with '/' ]) a directory
        if (Files.isDirectory(path) || pathString.endsWith("/")) {
            path = path.resolve("index.html");
        }

        if (Files.notExists(path)) {
            return new HttpResponse.Builder()
                    .setStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        // If the path is still a directory, fail
        if (Files.isDirectory(path)) {
            return null;
        }

        // Prevent escaping the root directory
        if (!isInsideRoot(path)) {
            return null;
        }

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            int fileSize = (int) channel.size();

            ByteBuffer fileBuffer = ByteBuffer.allocate(fileSize);
            channel.read(fileBuffer);

            return new HttpResponse.Builder()
                    .setStatus(HttpStatus.OK)
                    .setHeader("Content-Type", FileHelper.getContentType(path.toString()))
                    .setHeader("Content-Length", Integer.toString(fileSize))
                    .setBody(fileBuffer.array())
                    .build();
        } catch (IOException ioe) {
            request.getServer().getLogger().log(Level.WARNING, "Error writing response", ioe);

            return null;
        }
    }

    private boolean isInsideRoot(Path path) {
        try {
            // TODO: Could/Should this resolution and/or result be cached?
            return path.toRealPath(LinkOption.NOFOLLOW_LINKS)
                    .startsWith(root);
        } catch (IOException ioe) {
            // SECURITY: If the fully qualified path cannot be resolved, return false as a safety pre-caution
            return false;
        }
    }
}
