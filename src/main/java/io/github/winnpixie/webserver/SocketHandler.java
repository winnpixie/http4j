package io.github.winnpixie.webserver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketHandler {
    private final Socket socket;

    public SocketHandler(@NotNull Socket socket) {
        this.socket = socket;
    }

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
