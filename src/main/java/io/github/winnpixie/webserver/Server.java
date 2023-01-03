package io.github.winnpixie.webserver;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    private int port;
    private File rootDirectory;
    private boolean running;
    private Thread serverThread;

    public Server(int port, @NotNull File rootDirectory) {
        this.port = port;
        this.rootDirectory = rootDirectory;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (running) throw new RuntimeException("Can not change port while server is running!");

        this.port = port;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(@NotNull File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void startThread() {
        this.serverThread = new Thread(() -> {
            try (var srvSocket = new ServerSocket(port)) {
                System.out.printf("JWS started on %s:%d\n", srvSocket.getInetAddress().getHostName(), srvSocket.getLocalPort());

                while (running) {
                    var socket = srvSocket.accept();
                    socket.setSoTimeout(15000);
                    new RequestThread(this, socket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
    }
}
