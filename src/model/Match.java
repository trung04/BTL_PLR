package model;

import java.io.Serializable;
import java.util.Date;

/**
 * Class đại diện cho một trận đấu
 */
public class Match implements Serializable {
    private String matchId;
    private String player1Username;
    private String player2Username;
    private MatchStatus status;
    private long startTime;
    private int roundDuration; // Thời gian mỗi hiệp (giây)
    
    public enum MatchStatus {
        WAITING,    // Đang chờ bắt đầu
        IN_PROGRESS, // Đang diễn ra
        FINISHED,   // Đã kết thúc
        CANCELLED   // Bị hủy
    }

    public Match(String matchId, String player1Username, String player2Username) {
        this.matchId = matchId;
        this.player1Username = player1Username;
        this.player2Username = player2Username;
        this.status = MatchStatus.WAITING;
        this.startTime = System.currentTimeMillis();
        this.roundDuration = 60; // Mặc định 60 giây mỗi hiệp
    }

    // Getters and Setters
    public String getMatchId() {
        return matchId;
    }

    public String getPlayer1Username() {
        return player1Username;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    /**
     * Kiểm tra xem username có phải là người chơi trong trận này không
     */
    public boolean isPlayer(String username) {
        return player1Username.equals(username) || player2Username.equals(username);
    }

    /**
     * Lấy username của đối thủ
     */
    public String getOpponent(String username) {
        if (player1Username.equals(username)) {
            return player2Username;
        } else if (player2Username.equals(username)) {
            return player1Username;
        }
        return null;
    }
}
