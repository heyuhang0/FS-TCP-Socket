package com.hyh0.fstcpsocket.server;

import com.hyh0.fstcpsocket.socket.MessageHandler;
import com.hyh0.fstcpsocket.socket.TCPSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

public class TCPServer {

    private List<MessageHandler> handlers;
    private int port;
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

    public void accept() throws IOException {
        TCPSocket tcpSocket = new TCPSocket();
        for (MessageHandler h : handlers)
            tcpSocket.addHandler(h);
        tcpSocket.startEventLoop(serverSocket.accept());
    }
}
