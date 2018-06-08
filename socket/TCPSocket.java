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
import java.util.concurrent.ExecutorService;

public class TCPSocket {

    private BlockingQueue<String> outputQ;
    private Set<MessageHandler> handlers = new HashSet<>();

    private Socket socket;
    private PrintStream out;
    private BufferedReader buf;

    private volatile boolean closed = true;

    private Runnable sender;
    private Runnable receiver;

    /**
     * Build a TCPSocket with default send buffer size 32
     */
    public TCPSocket() {
        this(32);
    }

    /**
     * Build a TCPSocket
     * @param sendBufferSize    Size for send buffer where messages will be stored before actually being sent out
     */
    public TCPSocket(int sendBufferSize) {
        outputQ = new ArrayBlockingQueue<>(sendBufferSize);

        sender = () -> {
            try {
                while (isActive()) {
                    out.println(outputQ.take());
                }
            } catch (InterruptedException ignore) {
            } finally {
                close();
            }
        };

        receiver = () -> {
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
        };
    }


    public boolean isActive() {
        return !closed;
    }

    public synchronized void close() {
        if (isActive()) {
            closed = true;
            try {
                sendMessage(""); // notify sender so that it can shutdown
                out.close();
                buf.close(); // This will cause a IOException in receiver so that the thread will stop
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void sendMessage(String message) {
        try {
            outputQ.put(message);
        } catch (InterruptedException ignored) {}
    }

    public void addHandler(MessageHandler handler) {
        this.handlers.add(handler);
    }

    public void addHandlers(Iterable<MessageHandler> newHandlers) {
        newHandlers.forEach(this::addHandler);
    }

    public void removeHandler(MessageHandler handler) {
        handlers.remove(handler);
    }

    private boolean setupConnection(Socket socket) throws IOException {
        if (!isActive()) {
            this.closed = false;
            this.socket = socket;
            out = new PrintStream(socket.getOutputStream());
            buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;
        }
        return false;
    }

    public void startEventLoop(Socket socket) throws IOException {
        if (setupConnection(socket)) {
            new Thread(sender).start();
            new Thread(receiver).start();
        }
    }

    public void startEventLoop(Socket socket, ExecutorService threadPool) throws IOException {
        if (setupConnection(socket)) {
            threadPool.execute(sender);
            threadPool.execute(receiver);
        }
    }
}
