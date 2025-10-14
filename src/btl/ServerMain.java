/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package btl;


import controller.ServerController;

public class ServerMain {
    public static void main(String[] args) {
        ServerController server = new ServerController(2208);
        server.start();
    }
}