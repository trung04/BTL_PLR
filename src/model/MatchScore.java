package model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Lưu trữ điểm số của các người chơi trong trận
 */
public class MatchScore implements Serializable {
    private String matchId;
    private Map<String, Integer> scores; // username -> score
    
    public MatchScore(String matchId, String player1, String player2) {
        this.matchId = matchId;
        this.scores = new ConcurrentHashMap<>();
        this.scores.put(player1, 0);
        this.scores.put(player2, 0);
    }

    public void updateScore(String username, int score) {
        scores.put(username, score);
    }

    public void addScore(String username, int points) {
        scores.put(username, scores.getOrDefault(username, 0) + points);
    }

    public int getScore(String username) {
        return scores.getOrDefault(username, 0);
    }

    public String getMatchId() {
        return matchId;
    }

    public Map<String, Integer> getAllScores() {
        return new ConcurrentHashMap<>(scores);
    }

    /**
     * Lấy winner dựa trên điểm số hiện tại
     */
    public String getWinner() {
        String winner = null;
        int maxScore = -1;
        
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                winner = entry.getKey();
            }
        }
        
        return winner;
    }
}
