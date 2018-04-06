package com.hyh0.fstcpsocket.test;

import com.hyh0.fstcpsocket.server.TCPServer;

import java.io.IOException;

public class ServerTest {

    public static void main(String[] args) throws IOException {

        TCPServer server = new TCPServer.Builder()
                .port(4444)
                .withHandler((c, m) -> {
                    System.out.println(m);
                    c.sendMessage("Message received: " + m);
                    if ("close".equals(m))
                        c.close();
                    return true;
                }).build();

        while (true) {
            server.accept();
        }
    }
}
