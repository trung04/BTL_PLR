package model;

import java.io.Serializable;

/**
 * Class đại diện cho một lời mời chơi
 */
public class Invitation implements Serializable {
    private String invitationId;
    private String fromUsername;
    private String toUsername;
    private InvitationStatus status;
    private long createdTime;
    private static final long TIMEOUT_DURATION = 60000; // 60 giây timeout

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        TIMEOUT
    }

    public Invitation(String invitationId, String fromUsername, String toUsername) {
        this.invitationId = invitationId;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.status = InvitationStatus.PENDING;
        this.createdTime = System.currentTimeMillis();
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - createdTime > TIMEOUT_DURATION;
    }

    // Getters and Setters
    public String getInvitationId() {
        return invitationId;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public String getToUsername() {
        return toUsername;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public long getCreatedTime() {
        return createdTime;
    }
}
