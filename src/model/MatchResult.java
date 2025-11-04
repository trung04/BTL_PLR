package model;

import java.io.Serializable;

/**
 * Dữ liệu kết quả trận đấu gửi cho client
 */
public class MatchResult implements Serializable {
    private String matchId;
    private String winnerUsername;
    private String loserUsername;
    private int winnerScore;
    private int loserScore;
    private EndReason endReason;
    private long endTime;
    
    public enum EndReason {
        NORMAL_END,      // Kết thúc bình thường (hết thời gian)
        PLAYER_EXIT,     // Người chơi thoát
        PLAYER_DISCONNECT, // Mất kết nối
        CANCELLED        // Bị hủy
    }

    public MatchResult(String matchId, String winnerUsername, String loserUsername,
                      int winnerScore, int loserScore, EndReason endReason) {
        this.matchId = matchId;
        this.winnerUsername = winnerUsername;
        this.loserUsername = loserUsername;
        this.winnerScore = winnerScore;
        this.loserScore = loserScore;
        this.endReason = endReason;
        this.endTime = System.currentTimeMillis();
    }

    // Getters
    public String getMatchId() {
        return matchId;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    public String getLoserUsername() {
        return loserUsername;
    }

    public int getWinnerScore() {
        return winnerScore;
    }

    public int getLoserScore() {
        return loserScore;
    }

    public EndReason getEndReason() {
        return endReason;
    }

    public long getEndTime() {
        return endTime;
    }

    public boolean isPlayer(String username) {
        return winnerUsername.equals(username) || loserUsername.equals(username);
    }
}
