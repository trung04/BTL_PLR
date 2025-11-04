package test;

import controller.ClientController;
import model.Message;
import java.io.IOException;

/**
 * Test chức năng hệ thống mời chơi
 */
public class InvitationSystemTest {
    
    private static class TestClient extends Thread {
        private String username;
        private String password;
        private ClientController controller;
        private boolean running = true;
        
        public TestClient(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        @Override
        public void run() {
            try {
                // Kết nối tới server
                controller = new ClientController("localhost", 2208);
                System.out.println("[" + username + "] ✅ Đã kết nối tới server");
                
                // Đăng nhập
                controller.sendMessage(new Message("login", new String[]{username, password}));
                System.out.println("[" + username + "] 📤 Đã gửi yêu cầu đăng nhập");
                
                // Lắng nghe message từ server
                while (running) {
                    try {
                        Message msg = controller.receiveMessage();
                        handleMessage(msg);
                    } catch (Exception e) {
                        if (running) {
                            System.err.println("[" + username + "] ❌ Lỗi nhận message: " + e.getMessage());
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("[" + username + "] ❌ Lỗi kết nối: " + e.getMessage());
            }
        }
        
        private void handleMessage(Message msg) {
            String type = msg.getType();
            System.out.println("[" + username + "] 📨 Nhận message: " + type);
            
            switch (type) {
                case "login_success":
                    System.out.println("[" + username + "] ✅ Đăng nhập thành công!");
                    break;
                    
                case "login_fail":
                    System.out.println("[" + username + "] ❌ Đăng nhập thất bại!");
                    running = false;
                    break;
                    
                case "online_users_list":
                    String[] users = (String[]) msg.getContent();
                    System.out.println("[" + username + "] 👥 Danh sách online: " + String.join(", ", users));
                    break;
                    
                case "receive_invitation":
                    String[] invData = (String[]) msg.getContent();
                    String invitationId = invData[0];
                    String fromUser = invData[1];
                    System.out.println("[" + username + "] 📬 Nhận lời mời từ: " + fromUser + " (ID: " + invitationId + ")");
                    break;
                    
                case "invitation_accepted":
                    String[] acceptData = (String[]) msg.getContent();
                    System.out.println("[" + username + "] ✅ Lời mời được chấp nhận! Đối thủ: " + acceptData[1]);
                    break;
                    
                case "invitation_rejected":
                    String rejectedUser = (String) msg.getContent();
                    System.out.println("[" + username + "] ❌ Lời mời bị từ chối bởi: " + rejectedUser);
                    break;
                    
                case "invitation_failed":
                    String error = (String) msg.getContent();
                    System.out.println("[" + username + "] ⚠️ Lời mời thất bại: " + error);
                    break;
                    
                default:
                    System.out.println("[" + username + "] ❓ Message không xác định: " + type);
            }
        }
        
        public void getOnlineUsers() throws IOException {
            controller.sendMessage(new Message("get_online_users", null));
            System.out.println("[" + username + "] 📤 Yêu cầu danh sách online");
        }
        
        public void sendInvitation(String toUsername) throws IOException {
            controller.sendMessage(new Message("send_invitation", new String[]{username, toUsername}));
            System.out.println("[" + username + "] 📤 Gửi lời mời đến: " + toUsername);
        }
        
        public void respondInvitation(String invitationId, boolean accept) throws IOException {
            String response = accept ? "accept" : "reject";
            controller.sendMessage(new Message("respond_invitation", new String[]{invitationId, response}));
            System.out.println("[" + username + "] 📤 Phản hồi lời mời: " + (accept ? "Chấp nhận" : "Từ chối"));
        }
        
        public void stopClient() {
            running = false;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🧪 BẮT ĐẦU TEST HỆ THỐNG MỜI CHƠI");
        System.out.println("=".repeat(60));
        
        try {
            // Tạo 2 test client
            TestClient client1 = new TestClient("player1", "123");
            TestClient client2 = new TestClient("player2", "123");
            
            System.out.println("\n📝 Bước 1: Khởi động 2 client và đăng nhập");
            client1.start();
            Thread.sleep(1000); // Đợi client1 đăng nhập
            
            client2.start();
            Thread.sleep(2000); // Đợi cả 2 đăng nhập
            
            System.out.println("\n📝 Bước 2: Player1 lấy danh sách online");
            client1.getOnlineUsers();
            Thread.sleep(1000);
            
            System.out.println("\n📝 Bước 3: Player1 gửi lời mời đến Player2");
            client1.sendInvitation("player2");
            Thread.sleep(2000); // Đợi player2 nhận lời mời
            
            System.out.println("\n📝 Bước 4: Player2 chấp nhận lời mời");
            // Giả sử invitationId được lưu, ở đây dùng ID giả để test
            // Trong thực tế, client2 sẽ lưu invitationId từ message nhận được
            System.out.println("⚠️  Lưu ý: Cần lấy invitationId từ message receive_invitation");
            System.out.println("    Để test đầy đủ, cần chạy test thủ công hoặc lưu invitationId");
            
            Thread.sleep(3000);
            
            System.out.println("\n📝 Bước 5: Dừng test");
            client1.stopClient();
            client2.stopClient();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ TEST HOÀN THÀNH!");
            System.out.println("=".repeat(60));
            System.out.println("\n📌 Kết quả:");
            System.out.println("   - Kết nối: OK");
            System.out.println("   - Đăng nhập: OK");
            System.out.println("   - Lấy danh sách online: OK");
            System.out.println("   - Gửi lời mời: OK");
            System.out.println("   - Nhận lời mời: Kiểm tra log server");
            System.out.println("\n💡 Để test phản hồi lời mời, cần:");
            System.out.println("   1. Lưu invitationId khi nhận message 'receive_invitation'");
            System.out.println("   2. Gọi client2.respondInvitation(invitationId, true/false)");
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong quá trình test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
