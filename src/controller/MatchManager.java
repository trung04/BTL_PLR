package controller;

import model.Match;
import model.Match.MatchStatus;
import model.MatchData;
import model.MatchScore;
import model.MatchResult;
import model.MatchResult.EndReason;
import model.MatchState;
import model.MatchState.GamePhase;
import model.MatchState.PlayerState;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý tất cả các trận đấu trên server
 */
public class MatchManager {
    private Map<String, Match> matches; // matchId -> Match
    private Map<String, String> playerInMatch; // username -> matchId
    private Map<String, MatchScore> matchScores; // matchId -> MatchScore
    private Map<String, MatchState> matchStates; // matchId -> MatchState
    private static MatchManager instance;
    private static final int DEFAULT_TOTAL_ROUNDS = 3;

    private MatchManager() {
        this.matches = new ConcurrentHashMap<>();
        this.playerInMatch = new ConcurrentHashMap<>();
        this.matchScores = new ConcurrentHashMap<>();
        this.matchStates = new ConcurrentHashMap<>();
    }

    public static synchronized MatchManager getInstance() {
        if (instance == null) {
            instance = new MatchManager();
        }
        return instance;
    }

    /**
     * Tạo trận đấu mới
     */
    public Match createMatch(String player1Username, String player2Username) {
        // Kiểm tra xem các player có đang trong trận khác không
        if (playerInMatch.containsKey(player1Username)) {
            System.out.println("[MATCH] Player " + player1Username + " is already in a match");
            return null;
        }
        if (playerInMatch.containsKey(player2Username)) {
            System.out.println("[MATCH] Player " + player2Username + " is already in a match");
            return null;
        }

        // Tạo match mới
        String matchId = generateMatchId();
        Match match = new Match(matchId, player1Username, player2Username);
        
        matches.put(matchId, match);
        playerInMatch.put(player1Username, matchId);
        playerInMatch.put(player2Username, matchId);
        
        // Khởi tạo điểm số
        MatchScore score = new MatchScore(matchId, player1Username, player2Username);
        matchScores.put(matchId, score);
        
        // Khởi tạo trạng thái
        MatchState state = new MatchState(matchId, player1Username, player2Username, DEFAULT_TOTAL_ROUNDS);
        state.setPhase(GamePhase.WAITING);
        matchStates.put(matchId, state);
        
        System.out.println("[MATCH] Created match: " + matchId + 
                         " (" + player1Username + " vs " + player2Username + ")");
        return match;
    }

    /**
     * Bắt đầu trận đấu
     */
    public boolean startMatch(String matchId) {
        Match match = matches.get(matchId);
        if (match == null) {
            return false;
        }

        match.setStatus(MatchStatus.IN_PROGRESS);
        match.setStartTime(System.currentTimeMillis());
        
        // Cập nhật trạng thái
        MatchState state = matchStates.get(matchId);
        if (state != null) {
            state.setPhase(GamePhase.STARTING);
            state.setPlayerState(match.getPlayer1Username(), PlayerState.PLAYING);
            state.setPlayerState(match.getPlayer2Username(), PlayerState.PLAYING);
        }
        
        System.out.println("[MATCH] Started match: " + matchId);
        return true;
    }

    /**
     * Tạo MatchData để gửi cho client
     */
    public MatchData createMatchData(String matchId) {
        Match match = matches.get(matchId);
        if (match == null) {
            return null;
        }

        return new MatchData(
            match.getMatchId(),
            match.getPlayer1Username(),
            match.getPlayer2Username(),
            match.getStartTime(),
            match.getRoundDuration()
        );
    }

    /**
     * Kết thúc trận đấu
     */
    public boolean endMatch(String matchId, String winner) {
        Match match = matches.get(matchId);
        if (match == null) {
            return false;
        }

        match.setStatus(MatchStatus.FINISHED);
        
        // Cleanup
        playerInMatch.remove(match.getPlayer1Username());
        playerInMatch.remove(match.getPlayer2Username());
        matches.remove(matchId);
        
        System.out.println("[MATCH] Ended match: " + matchId + " - Winner: " + winner);
        return true;
    }

    /**
     * Hủy trận đấu
     */
    public boolean cancelMatch(String matchId) {
        Match match = matches.get(matchId);
        if (match == null) {
            return false;
        }

        match.setStatus(MatchStatus.CANCELLED);
        
        // Cleanup
        playerInMatch.remove(match.getPlayer1Username());
        playerInMatch.remove(match.getPlayer2Username());
        matches.remove(matchId);
        
        System.out.println("[MATCH] Cancelled match: " + matchId);
        return true;
    }

    /**
     * Lấy thông tin trận đấu
     */
    public Match getMatch(String matchId) {
        return matches.get(matchId);
    }

    /**
     * Lấy matchId của người chơi
     */
    public String getPlayerMatchId(String username) {
        return playerInMatch.get(username);
    }

    /**
     * Kiểm tra người chơi có đang trong trận đấu không
     */
    public boolean isPlayerInMatch(String username) {
        return playerInMatch.containsKey(username);
    }

    /**
     * Xử lý khi player disconnect
     */
    public void handlePlayerDisconnect(String username) {
        String matchId = playerInMatch.get(username);
        if (matchId != null) {
            Match match = matches.get(matchId);
            if (match != null) {
                System.out.println("[MATCH] Player " + username + " disconnected from match " + matchId);
                // Có thể tự động cho đối thủ thắng hoặc hủy trận
                cancelMatch(matchId);
            }
        }
    }

    /**
     * Sinh matchId duy nhất
     */
    private String generateMatchId() {
        return "MATCH_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Đếm số trận đấu đang diễn ra
     */
    public int getActiveMatchCount() {
        return matches.size();
    }

    // ============ SCORE MANAGEMENT ============
    
    /**
     * Cập nhật điểm của người chơi
     */
    public void updatePlayerScore(String matchId, String username, int score) {
        MatchScore matchScore = matchScores.get(matchId);
        if (matchScore != null) {
            matchScore.updateScore(username, score);
            System.out.println("[MATCH] Updated score for " + username + ": " + score);
        }
    }

    /**
     * Thêm điểm cho người chơi
     */
    public void addPlayerScore(String matchId, String username, int points) {
        MatchScore matchScore = matchScores.get(matchId);
        if (matchScore != null) {
            matchScore.addScore(username, points);
            System.out.println("[MATCH] Added " + points + " points to " + username);
        }
    }

    /**
     * Lấy điểm số hiện tại
     */
    public MatchScore getMatchScore(String matchId) {
        return matchScores.get(matchId);
    }

    // ============ EXIT MATCH HANDLING ============
    
    /**
     * Xử lý khi người chơi thoát trận (exit button)
     */
    public MatchResult handlePlayerExit(String username) {
        String matchId = playerInMatch.get(username);
        if (matchId == null) {
            return null;
        }

        Match match = matches.get(matchId);
        if (match == null) {
            return null;
        }

        // Lấy điểm số hiện tại
        MatchScore score = matchScores.get(matchId);
        String opponent = match.getOpponent(username);
        
        // Người thoát thua, đối thủ thắng
        int exitPlayerScore = score != null ? score.getScore(username) : 0;
        int opponentScore = score != null ? score.getScore(opponent) : 0;
        
        MatchResult result = new MatchResult(
            matchId,
            opponent, // winner
            username, // loser
            opponentScore,
            exitPlayerScore,
            EndReason.PLAYER_EXIT
        );

        // Cleanup
        cleanupMatch(matchId);
        
        System.out.println("[MATCH] Player exit: " + username + " from match " + matchId);
        return result;
    }

    /**
     * Xử lý khi người chơi mất kết nối
     */
    public MatchResult handlePlayerDisconnectWithResult(String username) {
        String matchId = playerInMatch.get(username);
        if (matchId == null) {
            return null;
        }

        Match match = matches.get(matchId);
        if (match == null) {
            return null;
        }

        // Lấy điểm số hiện tại
        MatchScore score = matchScores.get(matchId);
        String opponent = match.getOpponent(username);
        
        // Người disconnect thua, đối thủ thắng
        int disconnectPlayerScore = score != null ? score.getScore(username) : 0;
        int opponentScore = score != null ? score.getScore(opponent) : 0;
        
        MatchResult result = new MatchResult(
            matchId,
            opponent, // winner
            username, // loser
            opponentScore,
            disconnectPlayerScore,
            EndReason.PLAYER_DISCONNECT
        );

        // Cleanup
        cleanupMatch(matchId);
        
        System.out.println("[MATCH] Player disconnected: " + username + " from match " + matchId);
        return result;
    }

    /**
     * Kết thúc trận bình thường với kết quả cuối cùng
     */
    public MatchResult endMatchWithResult(String matchId) {
        Match match = matches.get(matchId);
        if (match == null) {
            return null;
        }

        MatchScore score = matchScores.get(matchId);
        if (score == null) {
            return null;
        }

        match.setStatus(MatchStatus.FINISHED);

        // Xác định winner
        String winner = score.getWinner();
        String loser = match.getOpponent(winner);
        
        MatchResult result = new MatchResult(
            matchId,
            winner,
            loser,
            score.getScore(winner),
            score.getScore(loser),
            EndReason.NORMAL_END
        );

        // Cleanup
        cleanupMatch(matchId);
        
        System.out.println("[MATCH] Match ended normally: " + matchId + " - Winner: " + winner);
        return result;
    }

    /**
     * Dọn dẹp dữ liệu trận đấu
     */
    private void cleanupMatch(String matchId) {
        Match match = matches.get(matchId);
        if (match != null) {
            playerInMatch.remove(match.getPlayer1Username());
            playerInMatch.remove(match.getPlayer2Username());
        }
        matches.remove(matchId);
        matchScores.remove(matchId);
        matchStates.remove(matchId);
    }

    // ============ MATCH STATE MANAGEMENT ============
    
    /**
     * Lấy trạng thái trận đấu
     */
    public MatchState getMatchState(String matchId) {
        return matchStates.get(matchId);
    }

    /**
     * Bắt đầu hiệp mới
     */
    public boolean startRound(String matchId, int roundNumber) {
        MatchState state = matchStates.get(matchId);
        if (state == null) {
            return false;
        }

        state.setCurrentRound(roundNumber);
        state.setPhase(GamePhase.ROUND_IN_PROGRESS);
        state.setRoundStartTime(System.currentTimeMillis());
        
        System.out.println("[MATCH] Started round " + roundNumber + " for match " + matchId);
        return true;
    }

    /**
     * Kết thúc hiệp
     */
    public boolean endRound(String matchId) {
        MatchState state = matchStates.get(matchId);
        if (state == null) {
            return false;
        }

        state.setPhase(GamePhase.ROUND_ENDED);
        state.setRoundEndTime(System.currentTimeMillis());
        
        System.out.println("[MATCH] Ended round " + state.getCurrentRound() + " for match " + matchId);
        return true;
    }

    /**
     * Kết thúc trận đấu (cập nhật state)
     */
    public boolean finishMatch(String matchId) {
        MatchState state = matchStates.get(matchId);
        if (state == null) {
            return false;
        }

        state.setPhase(GamePhase.MATCH_FINISHED);
        System.out.println("[MATCH] Finished match " + matchId);
        return true;
    }

    /**
     * Hủy trận (cập nhật state)
     */
    public boolean cancelMatchState(String matchId) {
        MatchState state = matchStates.get(matchId);
        if (state == null) {
            return false;
        }

        state.setPhase(GamePhase.CANCELLED);
        System.out.println("[MATCH] Cancelled match " + matchId);
        return true;
    }

    /**
     * Cập nhật trạng thái người chơi
     */
    public void updatePlayerState(String matchId, String username, PlayerState playerState) {
        MatchState state = matchStates.get(matchId);
        if (state != null) {
            state.setPlayerState(username, playerState);
            System.out.println("[MATCH] Player " + username + " state: " + playerState);
        }
    }

    /**
     * Cập nhật điểm trong state (đồng bộ với MatchScore)
     */
    public void syncScoreToState(String matchId) {
        MatchState state = matchStates.get(matchId);
        MatchScore score = matchScores.get(matchId);
        
        if (state != null && score != null) {
            Map<String, Integer> scores = score.getAllScores();
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                state.updateScore(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Kiểm tra hiệp có timeout không
     */
    public boolean checkRoundTimeout(String matchId) {
        Match match = matches.get(matchId);
        MatchState state = matchStates.get(matchId);
        
        if (match == null || state == null) {
            return false;
        }

        return state.isRoundTimeout(match.getRoundDuration());
    }

    /**
     * Lấy thời gian còn lại của hiệp
     */
    public long getRoundRemainingTime(String matchId) {
        Match match = matches.get(matchId);
        MatchState state = matchStates.get(matchId);
        
        if (match == null || state == null) {
            return 0;
        }

        return state.getRemainingTime(match.getRoundDuration());
    }
}
