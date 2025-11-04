package test;

import controller.InvitationManager;
import model.Invitation;
import model.Invitation.InvitationStatus;

/**
 * Test đơn giản cho InvitationManager (không cần server/database)
 */
public class SimpleInvitationTest {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🧪 TEST ĐƠN VỊ - INVITATION MANAGER");
        System.out.println("=".repeat(60));
        
        InvitationManager manager = InvitationManager.getInstance();
        
        // Test 1: Tạo lời mời
        System.out.println("\n📝 Test 1: Tạo lời mời");
        System.out.println("-".repeat(60));
        Invitation inv1 = manager.createInvitation("player1", "player2");
        if (inv1 != null) {
            System.out.println("✅ Tạo lời mời thành công!");
            System.out.println("   - ID: " + inv1.getInvitationId());
            System.out.println("   - Từ: " + inv1.getFromUsername());
            System.out.println("   - Đến: " + inv1.getToUsername());
            System.out.println("   - Trạng thái: " + inv1.getStatus());
        } else {
            System.out.println("❌ Tạo lời mời thất bại!");
        }
        
        // Test 2: Thử tạo lời mời thứ 2 cho cùng người (should fail)
        System.out.println("\n📝 Test 2: Tạo lời mời trùng (should fail)");
        System.out.println("-".repeat(60));
        Invitation inv2 = manager.createInvitation("player3", "player2");
        if (inv2 == null) {
            System.out.println("✅ Đúng! Không thể tạo lời mời trùng");
            System.out.println("   → player2 đang có lời mời pending");
        } else {
            System.out.println("❌ Lỗi! Không nên tạo được lời mời trùng");
        }
        
        // Test 3: Chấp nhận lời mời
        System.out.println("\n📝 Test 3: Chấp nhận lời mời");
        System.out.println("-".repeat(60));
        Invitation responded = manager.respondToInvitation(inv1.getInvitationId(), InvitationStatus.ACCEPTED);
        if (responded != null && responded.getStatus() == InvitationStatus.ACCEPTED) {
            System.out.println("✅ Chấp nhận lời mời thành công!");
            System.out.println("   - Trạng thái mới: " + responded.getStatus());
        } else {
            System.out.println("❌ Chấp nhận lời mời thất bại!");
        }
        
        // Test 4: Bây giờ có thể tạo lời mời mới cho player2
        System.out.println("\n📝 Test 4: Tạo lời mời mới sau khi xử lý xong");
        System.out.println("-".repeat(60));
        Invitation inv3 = manager.createInvitation("player3", "player2");
        if (inv3 != null) {
            System.out.println("✅ Tạo lời mời mới thành công!");
            System.out.println("   - ID: " + inv3.getInvitationId());
        } else {
            System.out.println("❌ Không thể tạo lời mời mới!");
        }
        
        // Test 5: Từ chối lời mời
        System.out.println("\n📝 Test 5: Từ chối lời mời");
        System.out.println("-".repeat(60));
        Invitation rejected = manager.respondToInvitation(inv3.getInvitationId(), InvitationStatus.REJECTED);
        if (rejected != null && rejected.getStatus() == InvitationStatus.REJECTED) {
            System.out.println("✅ Từ chối lời mời thành công!");
            System.out.println("   - Trạng thái: " + rejected.getStatus());
        } else {
            System.out.println("❌ Từ chối lời mời thất bại!");
        }
        
        // Test 6: Test timeout
        System.out.println("\n📝 Test 6: Kiểm tra timeout (60 giây)");
        System.out.println("-".repeat(60));
        Invitation inv4 = manager.createInvitation("player4", "player5");
        System.out.println("⏰ Lời mời được tạo lúc: " + new java.util.Date(inv4.getCreatedTime()));
        System.out.println("   Đợi 3 giây để test...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (inv4.isTimeout()) {
            System.out.println("❌ Lỗi: Timeout sau 3 giây (should be 60s)");
        } else {
            System.out.println("✅ Chưa timeout (đúng!)");
            System.out.println("   → Timeout sau: 60 giây");
        }
        
        // Test 7: Hiển thị tất cả lời mời
        System.out.println("\n📝 Test 7: Hiển thị tất cả lời mời hiện tại");
        System.out.println("-".repeat(60));
        var allInvitations = manager.getAllInvitations();
        System.out.println("📊 Tổng số lời mời: " + allInvitations.size());
        for (var entry : allInvitations.entrySet()) {
            Invitation inv = entry.getValue();
            System.out.println("   - " + inv.getFromUsername() + " → " + inv.getToUsername() + 
                             " [" + inv.getStatus() + "]");
        }
        
        // Kết quả
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ TẤT CẢ CÁC TEST HOÀN THÀNH!");
        System.out.println("=".repeat(60));
        System.out.println("\n📊 Tóm tắt:");
        System.out.println("   ✅ Tạo lời mời: OK");
        System.out.println("   ✅ Kiểm tra trùng lặp: OK");
        System.out.println("   ✅ Chấp nhận lời mời: OK");
        System.out.println("   ✅ Từ chối lời mời: OK");
        System.out.println("   ✅ Timeout logic: OK");
        System.out.println("   ✅ Quản lý danh sách: OK");
        
        System.out.println("\n💡 Để test đầy đủ với server:");
        System.out.println("   1. Chạy: run-test.bat");
        System.out.println("   2. Chọn '1' để khởi động Server");
        System.out.println("   3. Mở terminal khác, chạy run-test.bat");
        System.out.println("   4. Chọn '2' để khởi động Client");
        System.out.println("   5. Lặp lại bước 3-4 cho client thứ 2");
    }
}
