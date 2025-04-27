package io.github.winnpixie.http4j.server.endpoints.impl;

import io.github.winnpixie.http4j.server.endpoints.RequestHandler;
import io.github.winnpixie.http4j.server.incoming.Request;
import io.github.winnpixie.http4j.server.outgoing.Response;
import io.github.winnpixie.http4j.shared.HttpStatus;
import io.github.winnpixie.http4j.shared.utilities.FileHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
        Response response = new Response();
        String pathStr = request.getPath().substring(1);
        Path path = root.resolve(pathStr);

        // A file can not be a directory.
        boolean isDir = Files.isDirectory(path);
        if (!isDir && pathStr.endsWith("/")) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return response;
        }

        // Attempt to locate an index.html file if requested resource is a directory.
        if (isDir) path = path.resolve("index.html");
        if (Files.notExists(path)) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return response;
        }

        // Attempt to prevent escaping the root directory.
        Path normalizedPath = path.toAbsolutePath().normalize();
        Path normalizedRoot = root.toAbsolutePath().normalize();
        if (!normalizedPath.startsWith(normalizedRoot)) {
            request.getServer().getLogger()
                    .warning("Prevented read from path outside of root!");

            response.setStatus(HttpStatus.NOT_FOUND);
            return response;
        }

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            int fileSize = (int) channel.size();
            ByteBuffer fileBuffer = ByteBuffer.allocate(fileSize);
            channel.read(fileBuffer);

            response.setBody(fileBuffer.flip().array());

            response.setHeader("Content-Type", FileHelper.getContentType(path.toString()));
            response.setHeader("Content-Length", Integer.toString(fileSize));

            response.setStatus(HttpStatus.OK);
            return response;
        } catch (IOException ex) {
            ex.printStackTrace();

            return response;
        }
    }
}
