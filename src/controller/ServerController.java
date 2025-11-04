package controller;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerController {
    private int port;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private InvitationManager invitationManager;
    private MatchManager matchManager;

    public ServerController(int port) {
        this.port = port;
        this.invitationManager = InvitationManager.getInstance();
        this.matchManager = MatchManager.getInstance();
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

    // ============ INVITATION METHODS ============
    
    public synchronized void registerOnlineUser(String username, ClientHandler handler) {
        onlineUsers.put(username, handler);
        System.out.println("[SERVER] User online: " + username + " (Total: " + onlineUsers.size() + ")");
    }

    public synchronized void removeOnlineUser(String username) {
        onlineUsers.remove(username);
        invitationManager.cleanupTimeoutInvitations();
        
        // Xử lý disconnect khỏi match (nếu đang trong match)
        handlePlayerDisconnectFromMatch(username);
        
        System.out.println("[SERVER] User offline: " + username + " (Total: " + onlineUsers.size() + ")");
    }

    /**
     * Xử lý khi người chơi disconnect khỏi match
     */
    private void handlePlayerDisconnectFromMatch(String username) {
        model.MatchResult result = matchManager.handlePlayerDisconnectWithResult(username);
        
        if (result != null) {
            // Thông báo cho đối thủ
            String opponent = result.getWinnerUsername();
            ClientHandler opponentHandler = getClientHandler(opponent);
            
            if (opponentHandler != null) {
                opponentHandler.sendMessage(new model.Message("opponent_disconnected", result));
                opponentHandler.sendMessage(new model.Message("match_ended", result));
                System.out.println("[SERVER] Notified " + opponent + " about disconnect");
            }
        }
    }

    public synchronized List<String> getOnlineUsers() {
        return new ArrayList<>(onlineUsers.keySet());
    }

    public synchronized ClientHandler getClientHandler(String username) {
        return onlineUsers.get(username);
    }

    public InvitationManager getInvitationManager() {
        return invitationManager;
    }

    // ============ MATCH METHODS ============
    
    public MatchManager getMatchManager() {
        return matchManager;
    }

//    public synchronized void broadcast(String message) {
//        for (ClientHandler c : clients) {
//            c.sendMessage(message);
//        }
//    }
}
