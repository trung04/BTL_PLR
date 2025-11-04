package test;

import controller.InvitationManager;
import model.Invitation;
import model.Invitation.InvitationStatus;

/**
 * Test Invitation System (NO DATABASE REQUIRED)
 */
public class InvitationTest {
    
    public static void main(String[] args) {
        System.out.println("================================");
        System.out.println("   INVITATION SYSTEM TEST");
        System.out.println("================================\n");
        
        InvitationManager manager = InvitationManager.getInstance();
        
        // TEST 1: Create invitation
        System.out.println("[TEST 1] Create new invitation");
        System.out.println("----------------------------");
        Invitation inv1 = manager.createInvitation("player1", "player2");
        if (inv1 != null) {
            System.out.println("[SUCCESS] Invitation created!");
            System.out.println("   - ID: " + inv1.getInvitationId());
            System.out.println("   - From: " + inv1.getFromUsername());
            System.out.println("   - To: " + inv1.getToUsername());
            System.out.println("   - Status: " + inv1.getStatus());
        } else {
            System.out.println("[FAIL] Failed to create invitation!");
        }
        System.out.println();
        
        // TEST 2: Try to create duplicate invitation (player2 already has pending)
        System.out.println("[TEST 2] Try duplicate invitation to same player");
        System.out.println("----------------------------");
        Invitation inv2 = manager.createInvitation("player3", "player2");
        if (inv2 == null) {
            System.out.println("[SUCCESS] Correctly blocked duplicate invitation");
            System.out.println("   (player2 already has pending invitation from player1)");
        } else {
            System.out.println("[FAIL] Should not allow duplicate invitation!");
        }
        System.out.println();
        
        // TEST 3: Player2 accepts invitation
        System.out.println("[TEST 3] Player2 accepts invitation");
        System.out.println("----------------------------");
        Invitation accepted = manager.respondToInvitation(inv1.getInvitationId(), InvitationStatus.ACCEPTED);
        if (accepted != null && accepted.getStatus() == InvitationStatus.ACCEPTED) {
            System.out.println("[SUCCESS] Invitation accepted!");
            System.out.println("   - New status: " + accepted.getStatus());
            System.out.println("   - Can create match between " + accepted.getFromUsername() + " and " + accepted.getToUsername());
        }
        System.out.println();
        
        // TEST 4: Now player2 can receive new invitation
        System.out.println("[TEST 4] Player2 can receive new invitation");
        System.out.println("----------------------------");
        Invitation inv3 = manager.createInvitation("player3", "player2");
        if (inv3 != null) {
            System.out.println("[SUCCESS] New invitation created!");
            System.out.println("   - ID: " + inv3.getInvitationId());
            System.out.println("   - From: " + inv3.getFromUsername());
            System.out.println("   - To: " + inv3.getToUsername());
        }
        System.out.println();
        
        // TEST 5: Player2 rejects invitation
        System.out.println("[TEST 5] Player2 rejects invitation");
        System.out.println("----------------------------");
        Invitation rejected = manager.respondToInvitation(inv3.getInvitationId(), InvitationStatus.REJECTED);
        if (rejected != null && rejected.getStatus() == InvitationStatus.REJECTED) {
            System.out.println("[SUCCESS] Invitation rejected!");
            System.out.println("   - Status: " + rejected.getStatus());
            System.out.println("   - Player3 will be notified of rejection");
        }
        System.out.println();
        
        // TEST 6: Test timeout
        System.out.println("[TEST 6] Test timeout (simulation)");
        System.out.println("----------------------------");
        Invitation inv4 = manager.createInvitation("player4", "player5");
        System.out.println("[INFO] Invitation created. In real scenario, it will timeout after 60 seconds.");
        System.out.println("   - Current: " + inv4.getStatus());
        System.out.println("   - After 60s: Will change to TIMEOUT");
        System.out.println();
        
        // TEST 7: Display all invitations
        System.out.println("[TEST 7] List all invitations");
        System.out.println("----------------------------");
        System.out.println("Total invitations: " + manager.getAllInvitations().size());
        manager.getAllInvitations().forEach((id, inv) -> {
            System.out.println("   - " + inv.getFromUsername() + " -> " + inv.getToUsername() + " [" + inv.getStatus() + "]");
        });
        System.out.println();
        
        System.out.println("================================");
        System.out.println("   ALL TESTS COMPLETED!");
        System.out.println("================================");
    }
}
