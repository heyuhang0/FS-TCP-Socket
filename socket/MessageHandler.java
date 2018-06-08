package com.hyh0.fstcpsocket.socket;

@FunctionalInterface
public interface MessageHandler {
    /**
     * A handler for TCPSocket to handle income message
     * @param socket    TCPSocket that the message comes form
     * @param msg   Message received
     * @return  Whether the message has been handled
     */
    boolean handler(TCPSocket socket, String msg);
}

