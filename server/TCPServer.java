package com.hyh0.fstcpsocket.server;

import com.hyh0.fstcpsocket.socket.MessageHandler;
import com.hyh0.fstcpsocket.socket.TCPSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer {

    private List<MessageHandler> handlers;
    private final int port;
    private ServerSocket serverSocket;

    private TCPServer(Builder builder) throws IOException {
        this.handlers = builder.handlers;
        this.port = builder.port;
        this.serverSocket = new ServerSocket(port);
    }

    public static final class Builder {
        List<MessageHandler> handlers = new LinkedList<>();
        int port;

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder withHandler(MessageHandler handler) {
            this.handlers.add(handler);
            return this;
        }

        public TCPServer build() throws IOException {
            return new TCPServer(this);
        }
    }

    public int getPort() {
        return port;
    }

    /**
     * Accept a connection
     * @throws IOException  if an I/O error occurs when waiting for a connection
     * @deprecated Use start() and stop() methods to manage server threads as a group instead
     */
    public void accept() throws IOException {
        TCPSocket tcpSocket = new TCPSocket();
        tcpSocket.addHandlers(handlers);
        tcpSocket.startEventLoop(serverSocket.accept());
    }

    private volatile boolean running = false;
    private ExecutorService threadPool;

    public void start() {
        if (running)
            return;
        running = true;
        threadPool = Executors.newCachedThreadPool();
        new Thread(() -> {
            while (isRunning()) {
                TCPSocket tcpSocket = new TCPSocket();
                tcpSocket.addHandlers(handlers);
                try {
                    tcpSocket.startEventLoop(serverSocket.accept(), threadPool);
                } catch (IOException e) {
                    if (!serverSocket.isClosed())
                        e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ignore) {
        }
        threadPool.shutdownNow();
    }

    public boolean isRunning() {
        return running;
    }
}
