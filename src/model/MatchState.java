package model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Trạng thái chi tiết của một trận đấu
 */
public class MatchState implements Serializable {
    private String matchId;
    private GamePhase phase;
    private int currentRound;
    private int totalRounds;
    private long roundStartTime;
    private long roundEndTime;
    private Map<String, PlayerState> playerStates;
    private Map<String, Integer> scores;
    
    public enum GamePhase {
        WAITING("Waiting for players"),
        STARTING("Match starting"),
        ROUND_IN_PROGRESS("Round in progress"),
        ROUND_ENDED("Round ended"),
        MATCH_FINISHED("Match finished"),
        CANCELLED("Match cancelled");
        
        private String description;
        
        GamePhase(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum PlayerState {
        CONNECTED,
        PLAYING,
        DISCONNECTED,
        LEFT
    }

    public MatchState(String matchId, String player1, String player2, int totalRounds) {
        this.matchId = matchId;
        this.phase = GamePhase.WAITING;
        this.currentRound = 0;
        this.totalRounds = totalRounds;
        this.playerStates = new ConcurrentHashMap<>();
        this.scores = new ConcurrentHashMap<>();
        
        // Khởi tạo trạng thái người chơi
        playerStates.put(player1, PlayerState.CONNECTED);
        playerStates.put(player2, PlayerState.CONNECTED);
        
        // Khởi tạo điểm số
        scores.put(player1, 0);
        scores.put(player2, 0);
    }

    // Getters and Setters
    public String getMatchId() {
        return matchId;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public long getRoundEndTime() {
        return roundEndTime;
    }

    public void setRoundEndTime(long roundEndTime) {
        this.roundEndTime = roundEndTime;
    }

    public Map<String, PlayerState> getPlayerStates() {
        return new ConcurrentHashMap<>(playerStates);
    }

    public void setPlayerState(String username, PlayerState state) {
        playerStates.put(username, state);
    }

    public PlayerState getPlayerState(String username) {
        return playerStates.get(username);
    }

    public Map<String, Integer> getScores() {
        return new ConcurrentHashMap<>(scores);
    }

    public void updateScore(String username, int score) {
        scores.put(username, score);
    }

    public int getScore(String username) {
        return scores.getOrDefault(username, 0);
    }

    /**
     * Lấy thời gian còn lại của hiệp (giây)
     */
    public long getRemainingTime(int roundDuration) {
        if (phase != GamePhase.ROUND_IN_PROGRESS) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - roundStartTime;
        long remaining = (roundDuration * 1000L) - elapsed;
        return Math.max(0, remaining / 1000);
    }

    /**
     * Kiểm tra xem hiệp đã hết thời gian chưa
     */
    public boolean isRoundTimeout(int roundDuration) {
        if (phase != GamePhase.ROUND_IN_PROGRESS) {
            return false;
        }
        return System.currentTimeMillis() - roundStartTime >= (roundDuration * 1000L);
    }

    /**
     * Lấy thông tin trạng thái dạng text
     */
    public String getStatusText() {
        if (phase == GamePhase.ROUND_IN_PROGRESS) {
            return "Round " + currentRound + "/" + totalRounds + " - " + phase.getDescription();
        }
        return phase.getDescription();
    }

    /**
     * Clone để gửi cho client
     */
    public MatchState copy() {
        MatchState copy = new MatchState(matchId, "", "", totalRounds);
        copy.phase = this.phase;
        copy.currentRound = this.currentRound;
        copy.roundStartTime = this.roundStartTime;
        copy.roundEndTime = this.roundEndTime;
        copy.playerStates = new ConcurrentHashMap<>(this.playerStates);
        copy.scores = new ConcurrentHashMap<>(this.scores);
        return copy;
    }
}
