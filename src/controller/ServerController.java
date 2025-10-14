package controller;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerController {
    private int port;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();

    public ServerController(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("🚀 Server đang chạy trên cổng: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔗 Người chơi mới kết nối!");
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public synchronized void broadcast(String message) {
//        for (ClientHandler c : clients) {
//            c.sendMessage(message);
//        }
//    }
}
