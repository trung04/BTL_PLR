/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.io.*;
import java.net.*;
import model.Message;

/**
 *
 * @author D E L L
 */
public class ClientController {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientController(String host, int port) throws IOException {
        System.out.println("aa");
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void sendMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    public Message receiveMessage() throws IOException, ClassNotFoundException {
        return (Message) in.readObject();
    }
}
