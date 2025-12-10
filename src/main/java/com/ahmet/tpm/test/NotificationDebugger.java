package com.ahmet.tpm.test;

import com.ahmet.tpm.dao.NotificationDao;
import com.ahmet.tpm.dao.ProjectMemberDao;
import com.ahmet.tpm.dao.UserDao;
import com.ahmet.tpm.models.Notification;
import com.ahmet.tpm.models.User;
import com.ahmet.tpm.service.NotificationService;

import java.util.List;

/**
 * COMPLETE NOTIFICATION SYSTEM DEBUG SCRIPT
 * Run this to diagnose notification issues
 */
public class NotificationDebugger {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("  NOTIFICATION SYSTEM DIAGNOSTIC TEST");
        System.out.println("═══════════════════════════════════════════\n");

        NotificationDao notificationDao = new NotificationDao();
        NotificationService notificationService = new NotificationService();
        UserDao userDao = new UserDao();

        // ========== TEST 1: Check if Notifications table exists ==========
        System.out.println("TEST 1: Checking if Notifications table exists...");
        try {
            int count = notificationDao.getUnreadCount(1);
            System.out.println("✅ Notifications table EXISTS");
            System.out.println("   Current unread count for user 1: " + count);
        } catch (Exception e) {
            System.out.println("❌ FAILED: Notifications table does NOT exist or has error");
            System.out.println("   Error: " + e.getMessage());
            System.out.println("\n🔧 SOLUTION: Run the database_script.sql to create Notifications table");
            return;
        }

        System.out.println();

        // ========== TEST 2: Can we INSERT a notification? ==========
        System.out.println("TEST 2: Testing notification insertion...");
        try {
            Notification testNotif = new Notification();
            testNotif.setUserId(1);
            testNotif.setNotificationType("DEBUG_TEST");
            testNotif.setTitle("Debug Test Notification");
            testNotif.setMessage("This is a test notification created by the debugger");
            testNotif.setPriority("NORMAL");

            int notificationId = notificationDao.insert(testNotif);

            if (notificationId > 0) {
                System.out.println("✅ Notification insertion WORKS");
                System.out.println("   Created notification ID: " + notificationId);
            } else {
                System.out.println("❌ FAILED: Could not insert notification");
            }
        } catch (Exception e) {
            System.out.println("❌ FAILED: Error inserting notification");
            System.out.println("   Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();

        // ========== TEST 3: Can we READ notifications? ==========
        System.out.println("TEST 3: Testing notification retrieval...");
        try {
            List<Notification> notifications = notificationDao.getUserNotifications(1, 0, 10);
            System.out.println("✅ Notification retrieval WORKS");
            System.out.println("   Total notifications for user 1: " + notifications.size());

            if (notifications.size() > 0) {
                System.out.println("\n   📋 Recent notifications:");
                for (int i = 0; i < Math.min(3, notifications.size()); i++) {
                    Notification n = notifications.get(i);
                    System.out.println("   " + (i+1) + ". [" + n.getNotificationType() + "] " + n.getTitle());
                    System.out.println("      " + n.getMessage());
                    System.out.println("      Read: " + n.isRead() + " | Created: " + n.getCreatedAt());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ FAILED: Error reading notifications");
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println();

        // ========== TEST 4: Test NotificationService methods ==========
        System.out.println("TEST 4: Testing NotificationService...");
        try {
            // Test getting users
            User user1 = userDao.findById(1);
            if (user1 != null) {
                System.out.println("✅ User retrieval works");
                System.out.println("   User 1: " + user1.getFullName());

                // Create a test notification using service
                System.out.println("\n   Creating test notification via service...");
                notificationService.notifyProjectMemberAdded(
                        1,  // projectId
                        1,  // userId
                        "Test Project",
                        "System Test"
                );
                System.out.println("✅ NotificationService.notifyProjectMemberAdded() executed");
            } else {
                System.out.println("❌ Could not find user with ID 1");
            }
        } catch (Exception e) {
            System.out.println("❌ FAILED: Error in NotificationService");
            System.out.println("   Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();

        // ========== TEST 5: Check current unread count ==========
        System.out.println("TEST 5: Final unread count check...");
        try {
            int unreadCount = notificationDao.getUnreadCount(1);
            System.out.println("📊 Current unread notifications for user 1: " + unreadCount);

            if (unreadCount > 0) {
                System.out.println("\n✅ YOU SHOULD SEE A BADGE ON THE NOTIFICATION BELL!");
                System.out.println("   If you don't see it, the problem is in the UI, not the database.");
            } else {
                System.out.println("\n⚠️  No unread notifications found.");
                System.out.println("   This test created at least 2 notifications.");
                System.out.println("   Check if they were inserted correctly.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error checking unread count");
            System.out.println("   Error: " + e.getMessage());
        }

        System.out.println();
        System.out.println("═══════════════════════════════════════════");
        System.out.println("  DIAGNOSTIC COMPLETE");
        System.out.println("═══════════════════════════════════════════");

        // ========== RECOMMENDATIONS ==========
        System.out.println("\n📝 TROUBLESHOOTING GUIDE:");
        System.out.println();
        System.out.println("If notifications are in database but bell doesn't update:");
        System.out.println("  1. Check console for errors when clicking Edit/Add Member");
        System.out.println("  2. Verify mainFrame.getNotificationBell() is not null");
        System.out.println("  3. Check if refreshUnreadCount() is being called");
        System.out.println("  4. Restart the application (bell initializes on startup)");
        System.out.println();
        System.out.println("If notifications are NOT in database:");
        System.out.println("  1. Check if NotificationService methods are being called");
        System.out.println("  2. Look for SQLException errors in console");
        System.out.println("  3. Verify database connection is working");
        System.out.println("  4. Check if user/project IDs are correct");
        System.out.println();
        System.out.println("If TEST 1 fails:");
        System.out.println("  1. Run your database_script.sql");
        System.out.println("  2. Make sure Notifications table is created");
        System.out.println("  3. Check database connection in .env file");
    }
}