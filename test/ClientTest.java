package com.hyh0.fstcpsocket.test;

import com.hyh0.fstcpsocket.client.TCPClient;

import java.io.IOException;
import java.util.Scanner;

public class ClientTest {
    public static void main(String[] args) {
        // 获取地址端口
        String address;
        Scanner keyboardInput = new Scanner(System.in);
        int port;
        if (args.length >= 2) {
            address = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.print("端口号无效,请重新输入: ");
                port = keyboardInput.nextInt();
            }
        } else {
            System.out.print("请输入地址: ");
            address = keyboardInput.nextLine();
            System.out.print("请输入端口号: ");
            port = keyboardInput.nextInt();
        }

        try {
            TCPClient client = new TCPClient(address, port);
            client.addHandler((c, s) -> {System.out.println("-> " + s); return true;});
            client.startEventLoop();
            System.out.println("已连接" + address + ":" + port);
            while (client.isActive()) {
                String input = keyboardInput.nextLine();
                if (input != null && !"".equals(input))
                    client.sendMessage(input);
            }
            System.out.println("已断开连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}