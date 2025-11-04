# Hệ Thống Mời Chơi & Điều Khiển Trận Đấu

## Tổng Quan

Hệ thống bao gồm 2 phần chính:

### 1. Invitation System (Hệ thống mời chơi)
- Gửi lời mời đến người chơi online
- Chấp nhận hoặc từ chối lời mời
- Tự động timeout sau 60 giây
- Quản lý trạng thái lời mời (pending/accepted/rejected/timeout)

### 2. Match System (Hệ thống trận đấu)
- Tự động tạo trận đấu khi cả hai chấp nhận
- Đồng bộ thời gian và dữ liệu cho cả hai client
- Quản lý trạng thái trận đấu
- Xử lý disconnect và kết thúc trận

## Cấu Trúc Code

### 1. Model Classes

#### `Invitation.java`
- Đại diện cho một lời mời chơi
- Thuộc tính:
  - `invitationId`: ID duy nhất của lời mời
  - `fromUsername`: Người gửi lời mời
  - `toUsername`: Người nhận lời mời
  - `status`: Trạng thái (PENDING, ACCEPTED, REJECTED, TIMEOUT)
  - `createdTime`: Thời gian tạo
- Timeout: 60 giây

### 2. Controller Classes

#### `InvitationManager.java`
Singleton class quản lý tất cả lời mời trên server:

**Chức năng chính:**
- `createInvitation(fromUsername, toUsername)`: Tạo lời mời mới
- `respondToInvitation(invitationId, status)`: Xử lý phản hồi
- `getInvitation(invitationId)`: Lấy thông tin lời mời
- `cleanupTimeoutInvitations()`: Dọn dẹp lời mời hết hạn
- `cancelInvitation(invitationId, username)`: Hủy lời mời

#### `ServerController.java` (Đã cập nhật)
Thêm các phương thức:
- `registerOnlineUser(username, handler)`: Đăng ký user online
- `removeOnlineUser(username)`: Xóa user offline
- `getOnlineUsers()`: Lấy danh sách user online
- `getClientHandler(username)`: Lấy handler của user
- `getInvitationManager()`: Lấy invitation manager

#### `ClientHandler.java` (Đã cập nhật)
Thêm xử lý các message types mới:
- `send_invitation`: Gửi lời mời
- `respond_invitation`: Phản hồi lời mời
- `get_online_users`: Lấy danh sách user online

## Luồng Hoạt Động

### 1. Gửi Lời Mời
```
Client A → Server: Message("send_invitation", [usernameA, usernameB])
                ↓
          Server tạo Invitation
                ↓
Server → Client B: Message("receive_invitation", [invitationId, usernameA])
```

### 2. Chấp Nhận Lời Mời
```
Client B → Server: Message("respond_invitation", [invitationId, "accept"])
                ↓
        Server cập nhật status
                ↓
Server → Client A: Message("invitation_accepted", [invitationId, usernameB])
Server → Client B: Message("invitation_accepted", [invitationId, usernameA])
                ↓
          Tạo trận đấu mới
```

### 3. Từ Chối Lời Mời
```
Client B → Server: Message("respond_invitation", [invitationId, "reject"])
                ↓
        Server cập nhật status
                ↓
Server → Client A: Message("invitation_rejected", usernameB)
```

## Message Protocol

### Client → Server

1. **Gửi lời mời**
   ```java
   new Message("send_invitation", new String[]{fromUsername, toUsername})
   ```

2. **Phản hồi lời mời**
   ```java
   new Message("respond_invitation", new String[]{invitationId, "accept"}) // hoặc "reject"
   ```

3. **Lấy danh sách online**
   ```java
   new Message("get_online_users", null)
   ```

### Server → Client

1. **Nhận lời mời**
   ```java
   new Message("receive_invitation", new String[]{invitationId, fromUsername})
   ```

2. **Lời mời được chấp nhận**
   ```java
   new Message("invitation_accepted", new String[]{invitationId, otherUsername})
   ```

3. **Lời mời bị từ chối**
   ```java
   new Message("invitation_rejected", toUsername)
   ```

4. **Lời mời thất bại**
   ```java
   new Message("invitation_failed", errorMessage)
   ```

5. **Danh sách user online**
   ```java
   new Message("online_users_list", String[] usernames)
   ```

## Các Trường Hợp Đặc Biệt

1. **Người nhận không online**: Server gửi `invitation_failed` cho người gửi
2. **Người nhận đang có lời mời khác**: Server gửi `invitation_failed` cho người gửi
3. **Timeout (60s)**: Lời mời tự động chuyển sang trạng thái TIMEOUT
4. **Client disconnect**: Server tự động xóa user khỏi danh sách online

## Ghi Chú Kỹ Thuật

- Sử dụng `ConcurrentHashMap` để đảm bảo thread-safety
- InvitationManager là Singleton pattern
- Mỗi lời mời có UUID duy nhất
- Tự động cleanup khi disconnect
- Một user chỉ có thể có 1 lời mời pending tại một thời điểm

---

## PHẦN 2: MATCH SYSTEM

### Model Classes (Mới thêm)

#### `Match.java`
- Đại diện cho một trận đấu
- Thuộc tính:
  - `matchId`: ID duy nhất của trận đấu
  - `player1Username`, `player2Username`: Hai người chơi
  - `status`: WAITING, IN_PROGRESS, FINISHED, CANCELLED
  - `startTime`: Thời gian bắt đầu
  - `roundDuration`: Thời gian mỗi hiệp (giây)

#### `MatchData.java`
- Dữ liệu gửi cho client khi bắt đầu trận
- Chứa thông tin: matchId, players, time, serverTimestamp (để đồng bộ)

### Controller Classes (Mới thêm)

#### `MatchManager.java`
Singleton class quản lý tất cả trận đấu:

**Chức năng chính:**
- `createMatch(player1, player2)`: Tạo trận đấu mới
- `startMatch(matchId)`: Bắt đầu trận đấu
- `createMatchData(matchId)`: Tạo dữ liệu gửi cho client
- `endMatch(matchId, winner)`: Kết thúc trận đấu
- `cancelMatch(matchId)`: Hủy trận đấu
- `handlePlayerDisconnect(username)`: Xử lý khi player rời game

### Luồng Bắt Đầu Trận Đấu

```
1. Player A gửi lời mời → Player B
2. Player B chấp nhận
3. Server tạo Match tự động (MatchManager.createMatch)
4. Server sinh matchId duy nhất
5. Server tạo MatchData với:
   - matchId
   - player1Username, player2Username
   - startTime
   - roundDuration
   - serverTimestamp (để đồng bộ thời gian)
6. Server gửi Message("start_match", matchData) đến CẢ HAI client
7. Client nhận được → chuyển sang giao diện thi đấu
```

### Message Protocol (Thêm mới)

#### Server → Client (Match)

1. **Bắt đầu trận đấu**
   ```java
   new Message("start_match", matchData)
   ```
   MatchData bao gồm:
   - matchId: String
   - player1Username: String
   - player2Username: String
   - startTime: long (timestamp)
   - roundDuration: int (seconds)
   - serverTimestamp: long (để client đồng bộ)

2. **Lỗi tạo trận**
   ```java
   new Message("match_failed", errorMessage)
   ```

### Tích Hợp Với Các Phần Khác

#### Phối hợp với Đoàn (Server):
- **Đồng bộ vật phẩm rơi**: Đoàn có thể thêm logic sinh item trong `MatchManager.startMatch()`
- **Đồng bộ thời gian**: Sử dụng `serverTimestamp` trong MatchData
- **Gửi dữ liệu game**: Tạo thêm Message types như "item_spawn", "game_update"

#### Phối hợp với Võ (UI):
- **Khi nhận `start_match`**:
  ```java
  if (msg.getType().equals("start_match")) {
      MatchData data = (MatchData) msg.getContent();
      // Chuyển sang giao diện thi đấu
      // Load matchId, opponent username
      // Đồng bộ thời gian với serverTimestamp
      switchToGameScreen(data);
  }
  ```

### Code Example cho Client

```java
// Trong ClientController hoặc UI handler
private void handleStartMatch(Message msg) {
    MatchData matchData = (MatchData) msg.getContent();
    
    System.out.println("Match started: " + matchData.getMatchId());
    System.out.println("Opponent: " + getOpponent(matchData));
    System.out.println("Duration: " + matchData.getRoundDuration() + "s");
    
    // Đồng bộ thời gian
    long serverTime = matchData.getServerTimestamp();
    long localTime = System.currentTimeMillis();
    long timeDiff = serverTime - localTime;
    
    // Chuyển sang màn hình game
    Platform.runLater(() -> {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GameUI.fxml"));
            Parent root = loader.load();
            
            GameUIController gameController = loader.getController();
            gameController.initMatch(matchData, timeDiff);
            
            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    });
}

private String getOpponent(MatchData data) {
    if (currentUsername.equals(data.getPlayer1Username())) {
        return data.getPlayer2Username();
    }
    return data.getPlayer1Username();
}
```

### Xử Lý Disconnect

- Server tự động phát hiện disconnect trong `finally` block của `ClientHandler`
- Gọi `MatchManager.handlePlayerDisconnect()` để hủy trận đấu
- Có thể mở rộng để thông báo cho đối thủ

### Những Điều Cần Làm Tiếp Theo

#### Client Side (Võ - UI):
1. ✅ Nhận message "start_match"
2. ✅ Parse MatchData
3. ✅ Chuyển sang giao diện thi đấu
4. ✅ Hiển thị matchId, opponent, countdown timer
5. ✅ Đồng bộ thời gian với server

#### Server Side (Đoàn):
1. ✅ Thêm logic sinh vật phẩm rơi trong match
2. ✅ Broadcast vị trí items đến cả hai client
3. ✅ Xử lý điểm số và kết thúc hiệp
4. ✅ Gửi kết quả cuối cùng

### Trạng Thái Match

```
WAITING (Đang chờ) → khi vừa tạo
    ↓
IN_PROGRESS (Đang chơi) → sau startMatch()
    ↓
FINISHED (Kết thúc) → có người thắng
hoặc
CANCELLED (Hủy) → có người disconnect
```

---

## PHẦN 3: EXIT MATCH SYSTEM

### Model Classes (Mới thêm)

#### `MatchResult.java`
- Kết quả cuối cùng của trận đấu
- Thuộc tính:
  - `matchId`: ID trận đấu
  - `winnerUsername`, `loserUsername`: Người thắng/thua
  - `winnerScore`, `loserScore`: Điểm số
  - `endReason`: Lý do kết thúc
    - `NORMAL_END`: Kết thúc bình thường
    - `PLAYER_EXIT`: Người chơi thoát
    - `PLAYER_DISCONNECT`: Mất kết nối
    - `CANCELLED`: Bị hủy
  - `endTime`: Thời gian kết thúc

#### `MatchScore.java`
- Quản lý điểm số trong trận
- Methods:
  - `updateScore(username, score)`: Cập nhật điểm
  - `addScore(username, points)`: Thêm điểm
  - `getScore(username)`: Lấy điểm
  - `getWinner()`: Xác định người thắng

### MatchManager - Thêm Chức Năng Mới

**Score Management:**
- `updatePlayerScore(matchId, username, score)`: Cập nhật điểm
- `addPlayerScore(matchId, username, points)`: Thêm điểm
- `getMatchScore(matchId)`: Lấy điểm số hiện tại

**Exit Handling:**
- `handlePlayerExit(username)`: Xử lý người chơi thoát (nút Exit)
  - Người thoát thua
  - Đối thủ thắng
  - Tính điểm đến thời điểm đó
  - Trả về MatchResult

- `handlePlayerDisconnectWithResult(username)`: Xử lý disconnect
  - Tương tự exit nhưng reason = PLAYER_DISCONNECT
  - Tự động gọi khi phát hiện mất kết nối

- `endMatchWithResult(matchId)`: Kết thúc bình thường
  - So sánh điểm
  - Xác định winner
  - reason = NORMAL_END

### Luồng Xử Lý Thoát Trận

#### 1. Người Chơi Click "Exit"

```
Client → Server: Message("exit_match", username)
              ↓
Server xử lý (handleExitMatch):
  - Gọi MatchManager.handlePlayerExit()
  - Lấy điểm số hiện tại
  - Người thoát = thua, đối thủ = thắng
  - Tạo MatchResult
  - Cleanup: xóa khỏi playerInMatch
              ↓
Server → Client (người thoát): Message("match_ended", result)
Server → Đối thủ: Message("opponent_exited", result)
                  Message("match_ended", result)
              ↓
Cả hai client nhận kết quả → hiển thị màn hình kết quả
```

#### 2. Người Chơi Mất Kết Nối

```
Client disconnect (socket closed)
              ↓
ClientHandler.run() → finally block
              ↓
ServerController.removeOnlineUser(username)
              ↓
handlePlayerDisconnectFromMatch(username)
  - Gọi MatchManager.handlePlayerDisconnectWithResult()
  - Tạo MatchResult với reason = PLAYER_DISCONNECT
  - Cleanup match
              ↓
Server → Đối thủ: Message("opponent_disconnected", result)
                  Message("match_ended", result)
              ↓
Đối thủ thắng tự động
```

#### 3. Cập Nhật Điểm Số Trong Trận

```
Client → Server: Message("update_score", [matchId, username, score])
              ↓
Server: MatchManager.updatePlayerScore()
              ↓
Server broadcast đến CẢ HAI client:
  Message("score_updated", {player1: score1, player2: score2})
              ↓
Cả hai client cập nhật UI điểm số
```

### Message Protocol (Mới)

#### Client → Server

1. **Thoát trận**
   ```java
   new Message("exit_match", username)
   ```

2. **Cập nhật điểm**
   ```java
   new Message("update_score", new String[]{matchId, username, score})
   ```

#### Server → Client

1. **Trận đấu kết thúc**
   ```java
   new Message("match_ended", matchResult)
   ```
   MatchResult chứa: winner, loser, scores, endReason

2. **Đối thủ thoát**
   ```java
   new Message("opponent_exited", matchResult)
   ```

3. **Đối thủ disconnect**
   ```java
   new Message("opponent_disconnected", matchResult)
   ```

4. **Điểm số cập nhật**
   ```java
   new Message("score_updated", Map<String, Integer> scores)
   ```

### Đảm Bảo Không Bị Kẹt Trạng Thái "Bận"

✅ **Cleanup tự động:**
- Khi exit → `cleanupMatch()` xóa khỏi `playerInMatch`
- Khi disconnect → `handlePlayerDisconnectFromMatch()` cleanup
- Player có thể nhận lời mời mới ngay sau đó

✅ **Kiểm tra trước khi tạo match:**
- `MatchManager.createMatch()` kiểm tra `playerInMatch`
- Không cho tạo match nếu đang bận

✅ **Xử lý exception:**
- Tất cả operations có null check
- Finally block đảm bảo cleanup

### Code Example - Client Side

```java
// Xử lý nút Exit
btnExit.setOnAction(e -> {
    try {
        clientController.sendMessage(new Message("exit_match", currentUsername));
    } catch (IOException ex) {
        ex.printStackTrace();
    }
});

// Nhận kết quả trận đấu
private void handleMatchEnded(Message msg) {
    MatchResult result = (MatchResult) msg.getContent();
    
    String message;
    if (result.getWinnerUsername().equals(currentUsername)) {
        message = "YOU WIN!\n";
    } else {
        message = "YOU LOSE!\n";
    }
    
    message += "Score: " + result.getWinnerScore() + " - " + result.getLoserScore() + "\n";
    message += "Reason: " + result.getEndReason();
    
    // Hiển thị dialog kết quả
    Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Match Ended");
        alert.setHeaderText(message);
        alert.showAndWait();
        
        // Quay về lobby
        backToLobby();
    });
}

// Nhận thông báo đối thủ thoát
private void handleOpponentExited(Message msg) {
    MatchResult result = (MatchResult) msg.getContent();
    
    Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Opponent Left");
        alert.setHeaderText("Your opponent has left the match!");
        alert.setContentText("You win by default!");
        alert.showAndWait();
    });
}

// Cập nhật điểm số
private void updateScore(int newScore) {
    try {
        String[] data = {currentMatchId, currentUsername, String.valueOf(newScore)};
        clientController.sendMessage(new Message("update_score", data));
    } catch (IOException e) {
        e.printStackTrace();
    }
}

// Nhận điểm số cập nhật
private void handleScoreUpdated(Message msg) {
    Map<String, Integer> scores = (Map<String, Integer>) msg.getContent();
    
    Platform.runLater(() -> {
        lblMyScore.setText("Your Score: " + scores.get(currentUsername));
        lblOpponentScore.setText("Opponent: " + scores.get(opponentUsername));
    });
}
```

### Tích Hợp Với Đoàn (Server)

**Khi có vật phẩm được nhặt:**
```java
// Client gửi
new Message("item_collected", new String[]{matchId, username, itemId, points})

// Server xử lý
MatchManager.addPlayerScore(matchId, username, points);
// Broadcast điểm mới
```

**Khi hết thời gian:**
```java
// Server tự động
MatchResult result = MatchManager.endMatchWithResult(matchId);
// Gửi kết quả cho cả hai
```

### Checklist Hoàn Thành

✅ Tạo MatchResult và MatchScore models  
✅ Thêm score management vào MatchManager  
✅ Xử lý exit match (nút thoát)  
✅ Xử lý disconnect (mất kết nối)  
✅ Cleanup tự động để không bị kẹt  
✅ Thông báo đối thủ khi có người thoát  
✅ Tính điểm đến thời điểm thoát  
✅ Gửi kết quả cuối cùng cho cả hai  
✅ Broadcast điểm số real-time  

### Lưu Ý

- **Không sửa/xóa code cũ**: Tất cả chỉ THÊM mới
- **Thread-safe**: Dùng ConcurrentHashMap
- **Null-safe**: Kiểm tra null trước khi xử lý
✅ Cleanup đảm bảo**: Finally block + auto cleanup
```

---

## PHẦN 4: MATCH STATE MANAGEMENT

### Model Classes (Mới thêm)

#### `MatchState.java`
Quản lý trạng thái chi tiết của toàn bộ vòng đời trận đấu

**GamePhase (Giai đoạn):**
- `WAITING` - "Waiting for players"
- `STARTING` - "Match starting"
- `ROUND_IN_PROGRESS` - "Round in progress"
- `ROUND_ENDED` - "Round ended"
- `MATCH_FINISHED` - "Match finished"
- `CANCELLED` - "Match cancelled"

**PlayerState (Trạng thái người chơi):**
- `CONNECTED` - Đã kết nối
- `PLAYING` - Đang chơi
- `DISCONNECTED` - Mất kết nối
- `LEFT` - Đã thoát

**Thuộc tính:**
- `matchId`: ID trận đấu
- `phase`: Giai đoạn hiện tại
- `currentRound`: Hiệp hiện tại (1, 2, 3...)
- `totalRounds`: Tổng số hiệp (mặc định 3)
- `roundStartTime`: Thời điểm bắt đầu hiệp
- `roundEndTime`: Thời điểm kết thúc hiệp
- `playerStates`: Map<username, PlayerState>
- `scores`: Map<username, score>

**Methods:**
- `getRemainingTime(roundDuration)`: Lấy thời gian còn lại (giây)
- `isRoundTimeout(roundDuration)`: Kiểm tra timeout
- `getStatusText()`: Text hiển thị (VD: "Round 2/3 - Round in progress")
- `copy()`: Clone để gửi cho client

### MatchManager - Thêm Chức Năng State

**State Lifecycle:**
```
WAITING (tạo match)
    ↓
STARTING (startMatch)
    ↓
ROUND_IN_PROGRESS (startRound)
    ↓
ROUND_ENDED (endRound)
    ↓
ROUND_IN_PROGRESS (startRound tiếp theo)
    ↓ (lặp lại)
MATCH_FINISHED (endMatchWithResult)
hoặc
CANCELLED (disconnect/exit)
```

**Methods mới:**
- `getMatchState(matchId)`: Lấy state hiện tại
- `startRound(matchId, roundNumber)`: Bắt đầu hiệp
- `endRound(matchId)`: Kết thúc hiệp
- `finishMatch(matchId)`: Kết thúc trận
- `cancelMatchState(matchId)`: Hủy trận (update state)
- `updatePlayerState(matchId, username, state)`: Cập nhật trạng thái player
- `syncScoreToState(matchId)`: Đồng bộ điểm vào state
- `checkRoundTimeout(matchId)`: Kiểm tra timeout
- `getRoundRemainingTime(matchId)`: Lấy thời gian còn lại

### Luồng Cập Nhật Trạng Thái

#### 1. Tạo Match và Khởi Tạo State

```
createMatch(player1, player2)
    ↓
Tạo MatchState:
  - phase = WAITING
  - currentRound = 0
  - totalRounds = 3
  - playerStates = {p1: CONNECTED, p2: CONNECTED}
  - scores = {p1: 0, p2: 0}
```

#### 2. Bắt Đầu Match

```
startMatch(matchId)
    ↓
Update state:
  - phase = STARTING
  - playerStates = {p1: PLAYING, p2: PLAYING}
    ↓
Broadcast "match_state_update" → Cả hai client
```

#### 3. Bắt Đầu Hiệp

```
Server/Client: Message("start_round", [matchId, roundNumber])
    ↓
startRound(matchId, roundNumber)
  - currentRound = roundNumber
  - phase = ROUND_IN_PROGRESS
  - roundStartTime = now
    ↓
Broadcast "match_state_update" → Cả hai client
    ↓
Client hiển thị: "Round 1/3 - Round in progress"
```

#### 4. Trong Hiệp - Cập Nhật Điểm

```
Client: Message("update_score", [matchId, username, score])
    ↓
updatePlayerScore() + syncScoreToState()
    ↓
Broadcast "score_updated" + state.scores
    ↓
Client cập nhật UI điểm số real-time
```

#### 5. Kết Thúc Hiệp

```
Server (hoặc timeout checker): Message("end_round", matchId)
    ↓
endRound(matchId)
  - phase = ROUND_ENDED
  - roundEndTime = now
    ↓
Broadcast "match_state_update"
    ↓
Client hiển thị: "Round ended"
    ↓
Chờ vài giây → start round tiếp theo
```

#### 6. Kết Thúc Trận

```
endMatchWithResult(matchId)
    ↓
finishMatch(matchId)
  - phase = MATCH_FINISHED
    ↓
Broadcast "match_ended" + result
```

#### 7. Xử Lý Disconnect/Exit

```
handlePlayerExit() hoặc handlePlayerDisconnect()
    ↓
cancelMatchState(matchId)
  - phase = CANCELLED
    ↓
updatePlayerState(player, LEFT/DISCONNECTED)
    ↓
Broadcast "opponent_exited" + state
```

### Message Protocol (Mới)

#### Client → Server

1. **Bắt đầu hiệp**
   ```java
   new Message("start_round", new String[]{matchId, roundNumber})
   ```

2. **Kết thúc hiệp**
   ```java
   new Message("end_round", matchId)
   ```

3. **Lấy trạng thái**
   ```java
   new Message("get_match_state", matchId)
   ```

#### Server → Client

1. **Cập nhật trạng thái**
   ```java
   new Message("match_state_update", matchState)
   ```
   MatchState chứa:
   - phase: GamePhase
   - currentRound/totalRounds
   - playerStates: Map<username, state>
   - scores: Map<username, score>
   - roundStartTime
   
2. **Trạng thái cụ thể (theo yêu cầu)**
   ```java
   new Message("match_state", matchState)
   ```

### Code Example - Client Side

```java
// Nhận cập nhật trạng thái
private void handleMatchStateUpdate(Message msg) {
    MatchState state = (MatchState) msg.getContent();
    
    Platform.runLater(() -> {
        // Cập nhật UI phase
        lblPhase.setText(state.getStatusText());
        
        // Cập nhật round info
        if (state.getPhase() == GamePhase.ROUND_IN_PROGRESS) {
            lblRound.setText("Round " + state.getCurrentRound() + 
                           "/" + state.getTotalRounds());
            
            // Bắt đầu countdown timer
            startCountdownTimer(state.getRoundStartTime(), roundDuration);
        }
        
        // Cập nhật điểm số
        Map<String, Integer> scores = state.getScores();
        lblMyScore.setText("Your Score: " + scores.get(myUsername));
        lblOpponentScore.setText("Opponent: " + scores.get(opponentUsername));
        
        // Cập nhật trạng thái đối thủ
        PlayerState opponentState = state.getPlayerState(opponentUsername);
        if (opponentState == PlayerState.DISCONNECTED) {
            lblOpponentStatus.setText("Opponent disconnected!");
        } else if (opponentState == PlayerState.LEFT) {
            lblOpponentStatus.setText("Opponent left!");
        }
        
        // Xử lý theo phase
        switch (state.getPhase()) {
            case WAITING:
                showWaitingScreen();
                break;
            case STARTING:
                showStartingAnimation();
                break;
            case ROUND_IN_PROGRESS:
                enableGameplay();
                break;
            case ROUND_ENDED:
                showRoundResults();
                break;
            case MATCH_FINISHED:
                // Sẽ nhận "match_ended" message
                break;
            case CANCELLED:
                showCancelledMessage();
                break;
        }
    });
}

// Countdown timer
private void startCountdownTimer(long startTime, int duration) {
    if (countdownTimer != null) {
        countdownTimer.cancel();
    }
    
    countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = (duration * 1000L) - elapsed;
        
        if (remaining <= 0) {
            lblTimer.setText("Time's up!");
            countdownTimer.stop();
        } else {
            long seconds = remaining / 1000;
            lblTimer.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
        }
    }));
    
    countdownTimer.setCycleCount(Timeline.INDEFINITE);
    countdownTimer.play();
}

// Bắt đầu hiệp (từ server hoặc tự động)
private void requestStartRound(int roundNumber) {
    try {
        String[] data = {currentMatchId, String.valueOf(roundNumber)};
        clientController.sendMessage(new Message("start_round", data));
    } catch (IOException e) {
        e.printStackTrace();
    }
}

// Kết thúc hiệp
private void requestEndRound() {
    try {
        clientController.sendMessage(new Message("end_round", currentMatchId));
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

### Server - Auto Round Management (Đề xuất)

Có thể thêm vào ServerController hoặc MatchManager:

```java
// Tự động kiểm tra timeout và kết thúc hiệp
private void startRoundMonitor() {
    Timer timer = new Timer(true);
    timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
            for (String matchId : matches.keySet()) {
                if (checkRoundTimeout(matchId)) {
                    // Tự động kết thúc hiệp
                    endRound(matchId);
                    broadcastMatchState(matchId);
                    
                    // Kiểm tra xem có hiệp tiếp không
                    MatchState state = getMatchState(matchId);
                    if (state.getCurrentRound() < state.getTotalRounds()) {
                        // Chờ 3 giây rồi bắt đầu hiệp mới
                        scheduleNextRound(matchId, state.getCurrentRound() + 1);
                    } else {
                        // Kết thúc trận
                        MatchResult result = endMatchWithResult(matchId);
                        broadcastMatchEnd(result);
                    }
                }
            }
        }
    }, 1000, 1000); // Check mỗi giây
}
```

### Tích Hợp Với Team

**Đoàn (Server):**
- Implement auto round monitor
- Gọi `startRound()` khi bắt đầu hiệp mới
- Gọi `endRound()` khi timeout
- Broadcast state sau mỗi thay đổi quan trọng

**Võ (UI):**
- Listen `match_state_update`
- Cập nhật UI theo phase
- Hiển thị countdown timer
- Show "Round X/Y" info
- Hiển thị trạng thái đối thủ

### Checklist Hoàn Thành

✅ Tạo MatchState model với đầy đủ phases  
✅ Quản lý lifecycle: WAITING → PLAYING → FINISHED  
✅ Track currentRound và totalRounds  
✅ Theo dõi PlayerState cho mỗi người  
✅ Đồng bộ điểm số vào state  
✅ Tính thời gian còn lại của hiệp  
✅ Broadcast state update đến cả hai client  
✅ Xử lý timeout tự động  
✅ Cleanup state khi kết thúc  

### UI Examples Theo Phase

**WAITING:**
```
┌─────────────────────┐
│  Waiting for match  │
│       to start      │
│                     │
│    [Preparing...]   │
└─────────────────────┘
```

**ROUND_IN_PROGRESS:**
```
┌─────────────────────┐
│   Round 2/3         │
│   Time: 00:45       │
│                     │
│   You: 150          │
│   Opponent: 120     │
│                     │
│   [Game Area]       │
└─────────────────────┘
```

**ROUND_ENDED:**
```
┌─────────────────────┐
│   Round 2 Ended!    │
│                     │
│   You: 150          │
│   Opponent: 120     │
│                     │
│ Next round in 3...  │
└─────────────────────┘
```

**MATCH_FINISHED:**
```
┌─────────────────────┐
│   Match Finished!   │
│                     │
│   Final Score:      │
│   You: 450          │
│   Opponent: 380     │
│                     │
│   [YOU WIN!]        │
└─────────────────────┘
```

**Code không bị sửa/xóa - Chỉ THÊM mới!** ✅
```
