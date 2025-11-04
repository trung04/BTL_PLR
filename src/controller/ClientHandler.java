package controller;

import java.io.*;
import java.net.*;
import java.sql.*;
import database.DBConnection;
import model.Message;
import model.Invitation;
import model.Invitation.InvitationStatus;
import model.Match;
import model.MatchData;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ServerController server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String currentUsername;

    public ClientHandler(Socket socket, ServerController server) {
        this.socket = socket;
        this.server = server;
    }
    
    public String getCurrentUsername() {
        return currentUsername;
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
                } else if (msg.getType().equals("send_invitation")) {
                    handleSendInvitation(msg);
                } else if (msg.getType().equals("respond_invitation")) {
                    handleRespondInvitation(msg);
                } else if (msg.getType().equals("get_online_users")) {
                    handleGetOnlineUsers(msg);
                } else if (msg.getType().equals("exit_match")) {
                    handleExitMatch(msg);
                } else if (msg.getType().equals("update_score")) {
                    handleUpdateScore(msg);
                } else if (msg.getType().equals("start_round")) {
                    handleStartRound(msg);
                } else if (msg.getType().equals("end_round")) {
                    handleEndRound(msg);
                } else if (msg.getType().equals("get_match_state")) {
                    handleGetMatchState(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (currentUsername != null) {
                server.removeOnlineUser(currentUsername);
            }
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
                currentUsername = username;
                server.registerOnlineUser(username, this);
                sendMessage(new Message("login_success", new String[]{""}));
            } else {
                System.out.println("đã gửi login Fail");
                sendMessage(new Message("login_fail", new String[]{""}));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============ INVITATION HANDLERS ============
    
    private void handleSendInvitation(Message msg) {
        String[] data = (String[]) msg.getContent();
        String fromUsername = data[0];
        String toUsername = data[1];

        InvitationManager invManager = server.getInvitationManager();
        Invitation invitation = invManager.createInvitation(fromUsername, toUsername);

        if (invitation == null) {
            sendMessage(new Message("invitation_failed", "Player is busy or has pending invitation"));
            return;
        }

        ClientHandler targetHandler = server.getClientHandler(toUsername);
        if (targetHandler != null) {
            targetHandler.sendMessage(new Message("receive_invitation", 
                new String[]{invitation.getInvitationId(), fromUsername}));
            System.out.println("[INVITATION] Sent from " + fromUsername + " to " + toUsername);
        } else {
            sendMessage(new Message("invitation_failed", "Player is not online"));
        }
    }

    private void handleRespondInvitation(Message msg) {
        String[] data = (String[]) msg.getContent();
        String invitationId = data[0];
        String response = data[1];

        InvitationManager invManager = server.getInvitationManager();
        InvitationStatus status = response.equals("accept") ? 
            InvitationStatus.ACCEPTED : InvitationStatus.REJECTED;
        
        Invitation invitation = invManager.respondToInvitation(invitationId, status);

        if (invitation == null) {
            sendMessage(new Message("respond_failed", "Invitation not found"));
            return;
        }

        ClientHandler senderHandler = server.getClientHandler(invitation.getFromUsername());
        if (senderHandler != null) {
            if (status == InvitationStatus.ACCEPTED) {
                // Tạo trận đấu khi cả hai chấp nhận
                createAndStartMatch(invitation.getFromUsername(), invitation.getToUsername());
            } else {
                senderHandler.sendMessage(new Message("invitation_rejected", 
                    invitation.getToUsername()));
                System.out.println("[INVITATION] Rejected by " + invitation.getToUsername());
            }
        }
    }

    private void handleGetOnlineUsers(Message msg) {
        java.util.List<String> onlineUsers = server.getOnlineUsers();
        if (currentUsername != null) {
            onlineUsers.remove(currentUsername);
        }
        sendMessage(new Message("online_users_list", onlineUsers.toArray(new String[0])));
    }

    // ============ MATCH HANDLERS ============
    
    private void createAndStartMatch(String player1, String player2) {
        MatchManager matchManager = server.getMatchManager();
        
        // Tạo match
        Match match = matchManager.createMatch(player1, player2);
        if (match == null) {
            sendMessage(new Message("match_failed", "Failed to create match"));
            return;
        }

        // Bắt đầu match
        matchManager.startMatch(match.getMatchId());
        
        // Tạo MatchData để gửi cho cả hai client
        MatchData matchData = matchManager.createMatchData(match.getMatchId());
        
        // Gửi event startMatch đến cả hai client
        ClientHandler player1Handler = server.getClientHandler(player1);
        ClientHandler player2Handler = server.getClientHandler(player2);
        
        if (player1Handler != null) {
            player1Handler.sendMessage(new Message("start_match", matchData));
        }
        
        if (player2Handler != null) {
            player2Handler.sendMessage(new Message("start_match", matchData));
        }
        
        // Gửi trạng thái ban đầu
        model.MatchState initialState = matchManager.getMatchState(match.getMatchId());
        if (initialState != null) {
            Message stateMsg = new Message("match_state_update", initialState.copy());
            if (player1Handler != null) {
                player1Handler.sendMessage(stateMsg);
            }
            if (player2Handler != null) {
                player2Handler.sendMessage(stateMsg);
            }
        }
        
        System.out.println("[MATCH] Started match " + match.getMatchId() + 
                         " between " + player1 + " and " + player2);
    }

    // ============ EXIT MATCH HANDLERS ============
    
    /**
     * Xử lý khi người chơi thoát trận
     * Content: String username
     */
    private void handleExitMatch(Message msg) {
        String username = (String) msg.getContent();
        MatchManager matchManager = server.getMatchManager();
        
        // Xử lý thoát và lấy kết quả
        model.MatchResult result = matchManager.handlePlayerExit(username);
        
        if (result == null) {
            sendMessage(new Message("exit_failed", "Not in a match"));
            return;
        }

        // Gửi kết quả cho người thoát
        sendMessage(new Message("match_ended", result));
        
        // Thông báo cho đối thủ
        String opponent = result.getWinnerUsername();
        ClientHandler opponentHandler = server.getClientHandler(opponent);
        if (opponentHandler != null) {
            opponentHandler.sendMessage(new Message("opponent_exited", result));
            opponentHandler.sendMessage(new Message("match_ended", result));
        }
        
        System.out.println("[MATCH] Player " + username + " exited match " + result.getMatchId());
    }

    /**
     * Xử lý cập nhật điểm số trong trận
     * Content: String[] {matchId, username, score}
     */
    private void handleUpdateScore(Message msg) {
        String[] data = (String[]) msg.getContent();
        String matchId = data[0];
        String username = data[1];
        int score = Integer.parseInt(data[2]);
        
        MatchManager matchManager = server.getMatchManager();
        matchManager.updatePlayerScore(matchId, username, score);
        
        // Broadcast điểm mới cho cả hai người chơi
        Match match = matchManager.getMatch(matchId);
        if (match != null) {
            model.MatchScore matchScore = matchManager.getMatchScore(matchId);
            
            ClientHandler p1Handler = server.getClientHandler(match.getPlayer1Username());
            ClientHandler p2Handler = server.getClientHandler(match.getPlayer2Username());
            
            if (p1Handler != null) {
                p1Handler.sendMessage(new Message("score_updated", matchScore.getAllScores()));
            }
            if (p2Handler != null) {
                p2Handler.sendMessage(new Message("score_updated", matchScore.getAllScores()));
            }
            
            // Đồng bộ điểm vào state
            matchManager.syncScoreToState(matchId);
        }
    }

    // ============ MATCH STATE HANDLERS ============
    
    /**
     * Xử lý bắt đầu hiệp
     * Content: String[] {matchId, roundNumber}
     */
    private void handleStartRound(Message msg) {
        String[] data = (String[]) msg.getContent();
        String matchId = data[0];
        int roundNumber = Integer.parseInt(data[1]);
        
        MatchManager matchManager = server.getMatchManager();
        boolean success = matchManager.startRound(matchId, roundNumber);
        
        if (success) {
            // Broadcast trạng thái mới đến cả hai client
            broadcastMatchState(matchId);
        }
    }

    /**
     * Xử lý kết thúc hiệp
     * Content: String matchId
     */
    private void handleEndRound(Message msg) {
        String matchId = (String) msg.getContent();
        
        MatchManager matchManager = server.getMatchManager();
        boolean success = matchManager.endRound(matchId);
        
        if (success) {
            // Broadcast trạng thái mới đến cả hai client
            broadcastMatchState(matchId);
        }
    }

    /**
     * Xử lý yêu cầu lấy trạng thái trận đấu
     * Content: String matchId
     */
    private void handleGetMatchState(Message msg) {
        String matchId = (String) msg.getContent();
        
        MatchManager matchManager = server.getMatchManager();
        model.MatchState state = matchManager.getMatchState(matchId);
        
        if (state != null) {
            sendMessage(new Message("match_state", state.copy()));
        } else {
            sendMessage(new Message("state_not_found", "Match state not found"));
        }
    }

    /**
     * Broadcast trạng thái trận đấu đến cả hai người chơi
     */
    private void broadcastMatchState(String matchId) {
        MatchManager matchManager = server.getMatchManager();
        Match match = matchManager.getMatch(matchId);
        model.MatchState state = matchManager.getMatchState(matchId);
        
        if (match == null || state == null) {
            return;
        }

        // Gửi cho cả hai client
        ClientHandler p1Handler = server.getClientHandler(match.getPlayer1Username());
        ClientHandler p2Handler = server.getClientHandler(match.getPlayer2Username());
        
        Message stateMsg = new Message("match_state_update", state.copy());
        
        if (p1Handler != null) {
            p1Handler.sendMessage(stateMsg);
        }
        if (p2Handler != null) {
            p2Handler.sendMessage(stateMsg);
        }
        
        System.out.println("[MATCH] Broadcasted state: " + state.getStatusText());
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
    // Đổi từ private sang public để ServerController có thể gọi
    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
