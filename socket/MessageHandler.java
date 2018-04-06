package com.hyh0.fstcpsocket.socket;

@FunctionalInterface
public interface MessageHandler {
    boolean handler(TCPSocket socket, String msg);
}

