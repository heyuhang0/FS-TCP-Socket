package com.hyh0.fstcpsocket.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TCPSocket {

    private BlockingQueue<String> outputQ;
    private Set<MessageHandler> handlers = new HashSet<>();

    private Socket socket;
    private PrintStream out;
    private BufferedReader buf;

    private boolean closed;

    private Thread sendThread;
    private Thread receiveThread;

    public TCPSocket() {
        this(64);
    }

    public TCPSocket(int sendBufferSize) {
        outputQ = new ArrayBlockingQueue<>(sendBufferSize);

        sendThread = new Thread(() -> {
            try {
                while (isActive()) {
                    out.println(outputQ.take());
                }
            } catch (InterruptedException ignore) {
            } finally {
                close();
            }
        });

        receiveThread = new Thread(() -> {
            try {
                while (isActive()) {
                    String msg = buf.readLine();
                    if (msg == null) {
                        break;
                    }
                    handlers.forEach(l -> l.handler(this, msg));
                }
            } catch (IOException ignore) {
            } finally {
                close();
            }
        });
    }


    public boolean isActive() {
        return !closed;
    }

    public synchronized void close() {
        if (isActive()) {
            closed = true;
            out.close();
            sendThread.interrupt();
            try {
                buf.close();
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    public void sendMessage(String message) {
        try {
            outputQ.put(message);
        } catch (InterruptedException ignored) {}
    }

    public void addHandler(MessageHandler handler) {
        handlers.add(handler);
    }

    public void removeHandler(MessageHandler handler) {
        handlers.remove(handler);
    }

    public void startEventLoop(Socket socket) throws IOException {
        this.socket = socket;
        closed = false;
        out = new PrintStream(socket.getOutputStream());
        buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        sendThread.start();
        receiveThread.start();
    }
}
