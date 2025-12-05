package io.github.winnpixie.http4j.server.incoming.impl;

import io.github.winnpixie.http4j.server.incoming.Request;
import io.github.winnpixie.http4j.server.incoming.RequestHandler;
import io.github.winnpixie.http4j.server.outgoing.Response;
import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.utilities.FileHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;

public class FileRequestHandler extends RequestHandler {
    private Path root;

    public FileRequestHandler() {
        super("/");
    }

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        this.root = root;
    }

    @Override
    public Response process(Request request) {
        String pathStr = request.getPath().substring(1);
        Path path = root.resolve(pathStr);

        // A file can not be a directory.
        boolean isDir = Files.isDirectory(path);
        if (!isDir && pathStr.endsWith("/")) {
            return null;
        }

        // Attempt to locate an index.html file if requested resource is a directory.
        if (isDir) path = path.resolve("index.html");
        if (Files.notExists(path)) {
            return new Response.Builder()
                    .setStatus(HttpStatus.NOT_FOUND)
                    .build();
        }

        // Attempt to prevent escaping the root directory.
        Path normalizedPath = path.toAbsolutePath().normalize();
        Path normalizedRoot = root.toAbsolutePath().normalize();
        if (!normalizedPath.startsWith(normalizedRoot)) {
            return null;
        }

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = channel.size();

            ByteBuffer fileBuffer = ByteBuffer.allocate((int) fileSize);
            channel.read(fileBuffer);

            return new Response.Builder()
                    .setStatus(HttpStatus.OK)
                    .setHeader("Content-Type", FileHelper.getContentType(path.toString()))
                    .setHeader("Content-Length", Long.toString(fileSize))
                    .setBody((byte[]) fileBuffer.flip().array())
                    .build();
        } catch (IOException ioe) {
            request.getServer().getLogger().log(Level.WARNING, "Error writing request", ioe);

            return null;
        }
    }
}
