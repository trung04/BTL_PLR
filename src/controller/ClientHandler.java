package controller;

import java.io.*;
import java.net.*;
import java.sql.*;
import database.DBConnection;
import model.Message;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ServerController server;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket, ServerController server) {
        this.socket = socket;
        this.server = server;
    }
    @Override
    public void run() {
        try {
            System.out.println("đép zai");
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Message msg = (Message) in.readObject();
                if (msg.getType().equals("login")) {
                    handleLogin(msg);
                } 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(Message msg) {
        String[] credentials = (String[]) msg.getContent();
        String username = credentials[0];
        String password = credentials[1];
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("kết nối tới databse thành công");
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("đã gửi login success");
                sendMessage(new Message("login_success", new String[]{""}));
            } else {
                System.out.println("đã gửi login Fail");
                sendMessage(new Message("login_fail", new String[]{""}));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    private void handleRegister(Message msg) {
//        String username = msg.getContent()[0];
//        String password = msg.getContent()[1];
//
//        try (Connection conn = DBConnection.getConnection()) {
//            String check = "SELECT * FROM players WHERE username=?";
//            PreparedStatement psCheck = conn.prepareStatement(check);
//            psCheck.setString(1, username);
//            ResultSet rs = psCheck.executeQuery();
//            if (rs.next()) {
//                sendMessage(new Message("register_fail", new String[]{""}));
//                return;
//            }
//
//            String sql = "INSERT INTO players(username,password) VALUES(?,?)";
//            PreparedStatement ps = conn.prepareStatement(sql);
//            ps.setString(1, username);
//            ps.setString(2, password);
//            ps.executeUpdate();
//            sendMessage(new Message("register_success", new String[]{""}));
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
    private void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
