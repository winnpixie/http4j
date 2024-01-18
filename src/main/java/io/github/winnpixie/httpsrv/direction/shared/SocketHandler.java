package io.github.winnpixie.httpsrv.direction.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public record SocketHandler(@NotNull Socket socket) {
    @NotNull
    public Socket getSocket() {
        return socket;
    }

    @Nullable
    public InputStream getInputStream() {
        try {
            return socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public OutputStream getOutputStream() {
        try {
            return socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
