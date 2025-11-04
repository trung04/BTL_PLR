package test;

import controller.ClientController;
import model.Message;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

/**
 * Test thủ công cho hệ thống mời chơi
 * Chạy nhiều instance của class này để test với nhiều user
 */
public class ManualInvitationTest {
    
    private ClientController controller;
    private String username;
    private boolean running = true;
    private Map<String, String> pendingInvitations = new HashMap<>(); // fromUser -> invitationId
    
    public ManualInvitationTest(String host, int port, String username, String password) {
        this.username = username;
        
        try {
            // Kết nối và đăng nhập
            controller = new ClientController(host, port);
            System.out.println("✅ Đã kết nối tới server");
            
            controller.sendMessage(new Message("login", new String[]{username, password}));
            System.out.println("📤 Đã gửi yêu cầu đăng nhập với username: " + username);
            
            // Bắt đầu thread lắng nghe
            startListening();
            
        } catch (IOException e) {
            System.err.println("❌ Lỗi kết nối: " + e.getMessage());
        }
    }
    
    private void startListening() {
        new Thread(() -> {
            while (running) {
                try {
                    Message msg = controller.receiveMessage();
                    handleMessage(msg);
                } catch (Exception e) {
                    if (running) {
                        System.err.println("❌ Lỗi nhận message: " + e.getMessage());
                    }
                    break;
                }
            }
        }).start();
    }
    
    private void handleMessage(Message msg) {
        String type = msg.getType();
        
        switch (type) {
            case "login_success":
                System.out.println("\n✅ Đăng nhập thành công!");
                showMenu();
                break;
                
            case "login_fail":
                System.out.println("\n❌ Đăng nhập thất bại!");
                running = false;
                break;
                
            case "online_users_list":
                String[] users = (String[]) msg.getContent();
                System.out.println("\n👥 Danh sách người chơi online:");
                if (users.length == 0) {
                    System.out.println("   (Không có người chơi nào khác)");
                } else {
                    for (int i = 0; i < users.length; i++) {
                        System.out.println("   " + (i + 1) + ". " + users[i]);
                    }
                }
                break;
                
            case "receive_invitation":
                String[] invData = (String[]) msg.getContent();
                String invitationId = invData[0];
                String fromUser = invData[1];
                pendingInvitations.put(fromUser, invitationId);
                System.out.println("\n📬 🔔 BẠN CÓ LỜI MỜI MỚI!");
                System.out.println("   Từ: " + fromUser);
                System.out.println("   InvitationID: " + invitationId);
                System.out.println("   Nhập '3' để chấp nhận hoặc '4' để từ chối");
                break;
                
            case "invitation_accepted":
                String[] acceptData = (String[]) msg.getContent();
                System.out.println("\n🎉 ✅ LỜI MỜI ĐƯỢC CHẤP NHẬN!");
                System.out.println("   InvitationID: " + acceptData[0]);
                System.out.println("   Đối thủ: " + acceptData[1]);
                System.out.println("   → Có thể bắt đầu trận đấu!");
                break;
                
            case "invitation_rejected":
                String rejectedUser = (String) msg.getContent();
                System.out.println("\n❌ Lời mời bị từ chối bởi: " + rejectedUser);
                break;
                
            case "invitation_failed":
                String error = (String) msg.getContent();
                System.out.println("\n⚠️ Lời mời thất bại: " + error);
                break;
                
            default:
                System.out.println("\n❓ Message: " + type);
        }
        
        System.out.print("\nNhập lựa chọn: ");
    }
    
    private void showMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🎮 MENU - " + username);
        System.out.println("=".repeat(50));
        System.out.println("1. Xem danh sách người chơi online");
        System.out.println("2. Gửi lời mời chơi");
        System.out.println("3. Chấp nhận lời mời");
        System.out.println("4. Từ chối lời mời");
        System.out.println("5. Xem lời mời đang chờ");
        System.out.println("0. Thoát");
        System.out.println("=".repeat(50));
    }
    
    public void run() {
        Scanner scanner = new Scanner(System.in);
        
        while (running) {
            try {
                System.out.print("\nNhập lựa chọn: ");
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        controller.sendMessage(new Message("get_online_users", null));
                        System.out.println("📤 Đang lấy danh sách...");
                        break;
                        
                    case "2":
                        System.out.print("Nhập username người chơi muốn mời: ");
                        String toUser = scanner.nextLine().trim();
                        controller.sendMessage(new Message("send_invitation", new String[]{username, toUser}));
                        System.out.println("📤 Đã gửi lời mời đến: " + toUser);
                        break;
                        
                    case "3":
                        if (pendingInvitations.isEmpty()) {
                            System.out.println("⚠️ Không có lời mời nào!");
                        } else {
                            System.out.println("Danh sách lời mời:");
                            int idx = 1;
                            for (String from : pendingInvitations.keySet()) {
                                System.out.println("   " + idx + ". Từ " + from + " (ID: " + pendingInvitations.get(from) + ")");
                                idx++;
                            }
                            System.out.print("Nhập username người gửi để chấp nhận: ");
                            String acceptFrom = scanner.nextLine().trim();
                            String invId = pendingInvitations.get(acceptFrom);
                            if (invId != null) {
                                controller.sendMessage(new Message("respond_invitation", new String[]{invId, "accept"}));
                                System.out.println("✅ Đã chấp nhận lời mời từ: " + acceptFrom);
                                pendingInvitations.remove(acceptFrom);
                            } else {
                                System.out.println("❌ Không tìm thấy lời mời từ: " + acceptFrom);
                            }
                        }
                        break;
                        
                    case "4":
                        if (pendingInvitations.isEmpty()) {
                            System.out.println("⚠️ Không có lời mời nào!");
                        } else {
                            System.out.println("Danh sách lời mời:");
                            int idx = 1;
                            for (String from : pendingInvitations.keySet()) {
                                System.out.println("   " + idx + ". Từ " + from + " (ID: " + pendingInvitations.get(from) + ")");
                                idx++;
                            }
                            System.out.print("Nhập username người gửi để từ chối: ");
                            String rejectFrom = scanner.nextLine().trim();
                            String invId = pendingInvitations.get(rejectFrom);
                            if (invId != null) {
                                controller.sendMessage(new Message("respond_invitation", new String[]{invId, "reject"}));
                                System.out.println("❌ Đã từ chối lời mời từ: " + rejectFrom);
                                pendingInvitations.remove(rejectFrom);
                            } else {
                                System.out.println("❌ Không tìm thấy lời mời từ: " + rejectFrom);
                            }
                        }
                        break;
                        
                    case "5":
                        if (pendingInvitations.isEmpty()) {
                            System.out.println("📭 Không có lời mời nào đang chờ");
                        } else {
                            System.out.println("📬 Lời mời đang chờ:");
                            for (Map.Entry<String, String> entry : pendingInvitations.entrySet()) {
                                System.out.println("   - Từ: " + entry.getKey() + " (ID: " + entry.getValue() + ")");
                            }
                        }
                        break;
                        
                    case "0":
                        System.out.println("👋 Đang thoát...");
                        running = false;
                        break;
                        
                    case "menu":
                        showMenu();
                        break;
                        
                    default:
                        System.out.println("❌ Lựa chọn không hợp lệ! Nhập 'menu' để xem lại menu.");
                }
                
            } catch (Exception e) {
                System.err.println("❌ Lỗi: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🧪 TEST THỦ CÔNG HỆ THỐNG MỜI CHƠI");
        System.out.println("=".repeat(60));
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Nhập username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Nhập password: ");
        String password = scanner.nextLine().trim();
        
        System.out.println("\n🔌 Đang kết nối tới server localhost:2208...");
        
        ManualInvitationTest test = new ManualInvitationTest("localhost", 2208, username, password);
        test.run();
        
        scanner.close();
        System.out.println("\n✅ Test kết thúc!");
    }
}
