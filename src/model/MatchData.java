package model;

import java.io.Serializable;

/**
 * Class chứa dữ liệu để bắt đầu trận đấu gửi cho client
 */
public class MatchData implements Serializable {
    private String matchId;
    private String player1Username;
    private String player2Username;
    private long startTime;
    private int roundDuration;
    private long serverTimestamp; // Timestamp của server để đồng bộ

    public MatchData(String matchId, String player1Username, String player2Username, 
                     long startTime, int roundDuration) {
        this.matchId = matchId;
        this.player1Username = player1Username;
        this.player2Username = player2Username;
        this.startTime = startTime;
        this.roundDuration = roundDuration;
        this.serverTimestamp = System.currentTimeMillis();
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

    public long getStartTime() {
        return startTime;
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public long getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }
}
