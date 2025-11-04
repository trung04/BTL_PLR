package controller;

import model.Invitation;
import model.Invitation.InvitationStatus;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý tất cả các lời mời chơi trên server
 */
public class InvitationManager {
    private Map<String, Invitation> invitations; // invitationId -> Invitation
    private Map<String, String> pendingInvitations; // toUsername -> invitationId
    private static InvitationManager instance;

    private InvitationManager() {
        this.invitations = new ConcurrentHashMap<>();
        this.pendingInvitations = new ConcurrentHashMap<>();
    }

    public static synchronized InvitationManager getInstance() {
        if (instance == null) {
            instance = new InvitationManager();
        }
        return instance;
    }

    /**
     * Tạo lời mời mới
     */
    public Invitation createInvitation(String fromUsername, String toUsername) {
        // Kiểm tra xem người nhận có đang có lời mời pending không
        if (pendingInvitations.containsKey(toUsername)) {
            return null; // Người này đang có lời mời pending
        }

        String invitationId = UUID.randomUUID().toString();
        Invitation invitation = new Invitation(invitationId, fromUsername, toUsername);
        
        invitations.put(invitationId, invitation);
        pendingInvitations.put(toUsername, invitationId);
        
        System.out.println("✉️ Lời mời mới: " + fromUsername + " -> " + toUsername);
        return invitation;
    }

    /**
     * Xử lý phản hồi từ người nhận
     */
    public Invitation respondToInvitation(String invitationId, InvitationStatus response) {
        Invitation invitation = invitations.get(invitationId);
        
        if (invitation == null) {
            return null;
        }

        // Kiểm tra timeout
        if (invitation.isTimeout()) {
            invitation.setStatus(InvitationStatus.TIMEOUT);
            cleanupInvitation(invitationId);
            System.out.println("⏰ Lời mời đã hết hạn: " + invitationId);
            return invitation;
        }

        // Cập nhật status
        invitation.setStatus(response);
        
        // Xóa khỏi pending
        pendingInvitations.remove(invitation.getToUsername());
        
        System.out.println("📬 Phản hồi lời mời: " + invitationId + " - " + response);
        return invitation;
    }

    /**
     * Lấy thông tin lời mời
     */
    public Invitation getInvitation(String invitationId) {
        return invitations.get(invitationId);
    }

    /**
     * Kiểm tra và xóa các lời mời timeout
     */
    public void cleanupTimeoutInvitations() {
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, Invitation> entry : invitations.entrySet()) {
            Invitation inv = entry.getValue();
            if (inv.getStatus() == InvitationStatus.PENDING && inv.isTimeout()) {
                inv.setStatus(InvitationStatus.TIMEOUT);
                toRemove.add(entry.getKey());
            }
        }
        
        for (String id : toRemove) {
            cleanupInvitation(id);
        }
    }

    /**
     * Xóa lời mời sau khi xử lý xong
     */
    private void cleanupInvitation(String invitationId) {
        Invitation inv = invitations.remove(invitationId);
        if (inv != null) {
            pendingInvitations.remove(inv.getToUsername());
        }
    }

    /**
     * Hủy lời mời (từ người gửi)
     */
    public boolean cancelInvitation(String invitationId, String username) {
        Invitation inv = invitations.get(invitationId);
        if (inv != null && inv.getFromUsername().equals(username)) {
            cleanupInvitation(invitationId);
            return true;
        }
        return false;
    }

    /**
     * Lấy danh sách tất cả lời mời (cho debug)
     */
    public Map<String, Invitation> getAllInvitations() {
        return new HashMap<>(invitations);
    }
}
