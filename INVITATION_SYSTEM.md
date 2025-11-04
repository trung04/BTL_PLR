# Hệ Thống Mời Chơi (Invitation System)

## Tổng Quan

Hệ thống cho phép người chơi mời nhau tạo trận đấu với các tính năng:
- Gửi lời mời đến người chơi online
- Chấp nhận hoặc từ chối lời mời
- Tự động timeout sau 60 giây
- Quản lý trạng thái lời mời (pending/accepted/rejected/timeout)

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

## Những Điều Cần Làm Tiếp Theo (Client Side)

1. Tạo UI để hiển thị danh sách user online
2. Tạo UI để gửi/nhận lời mời
3. Xử lý các message từ server
4. Tạo logic khởi tạo game khi cả hai chấp nhận
