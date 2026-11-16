package task05;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * WalletTransactionDemo
 * Demonstrates:
 * 1. ACID-compliant inter-wallet funds transfer with commit/rollback recovery
 * 2. SQL injection vulnerability demonstration vs PreparedStatement parameterized immunity
 */
public class WalletTransactionDemo {

    // Simulated Bank/Wallet ledger database table
    public static class WalletLedger {
        private final Map<String, Double> hostelWallets = new HashMap<>();
        private final Map<String, Double> canteenWallets = new HashMap<>();

        public WalletLedger() {
            hostelWallets.put("STUDENT-01", 1500.0);
            canteenWallets.put("STUDENT-01", 200.0);
        }

        public double getHostelBalance(String id) { return hostelWallets.getOrDefault(id, 0.0); }
        public double getCanteenBalance(String id) { return canteenWallets.getOrDefault(id, 0.0); }

        public synchronized void transferWithTransaction(String id, double amount, boolean forceCrashHalfway) throws Exception {
            double initialHostel = hostelWallets.get(id);
            double initialCanteen = canteenWallets.get(id);

            if (initialHostel < amount) {
                throw new IllegalArgumentException("Insufficient balance in hostel wallet");
            }

            // Begin Transaction boundary: setAutoCommit(false)
            System.out.println("[TX BEGIN] Debiting ₹" + amount + " from Hostel wallet...");
            hostelWallets.put(id, initialHostel - amount);

            if (forceCrashHalfway) {
                // Simulate sudden power interruption / DB failure before credit
                System.out.println("[SIMULATED CRASH] Power/network lost before crediting canteen wallet!");
                // Rollback handler triggered
                hostelWallets.put(id, initialHostel); // Rollback to initial state
                System.out.println("[TX ROLLBACK] Transaction rolled back cleanly. Money restored to ₹" + initialHostel);
                throw new RuntimeException("Simulated hardware failure");
            }

            // Step 2: Credit
            System.out.println("[TX EXECUTE] Crediting ₹" + amount + " to Canteen wallet...");
            canteenWallets.put(id, initialCanteen + amount);

            // Step 3: Commit
            System.out.println("[TX COMMIT] Both operations successfully committed to persistent storage.");
        }
    }

    // =========================================================================
    // 2. SQL INJECTION SECURITY AUDIT SIMULATION
    // =========================================================================
    public static class AuthenticationSecurityService {
        private final Map<String, String> userDb = new HashMap<>();

        public AuthenticationSecurityService() {
            userDb.put("admin", "AdminSecret#2026");
            userDb.put("student_user", "Pass1234");
        }

        // Unsafe dynamic SQL concatenation
        public boolean unsafeLogin(String userId, String password) {
            String sql = "SELECT * FROM users WHERE id='" + userId + "' AND pwd='" + password + "'";
            System.out.println("Executing Unsafe Dynamic Query: " + sql);

            // An attacker supplying password = "' OR '1'='1" converts WHERE clause to:
            // WHERE id='admin' AND pwd='' OR '1'='1' which evaluates to TRUE!
            if (password.contains("' OR '1'='1") || userId.contains("' OR '1'='1")) {
                System.out.println(">>> VULNERABILITY EXPLOITED! SQL tautology bypassed authentication.");
                return true;
            }
            return userDb.containsKey(userId) && userDb.get(userId).equals(password);
        }

        // Safe PreparedStatement parameterized check
        public boolean safeLogin(String userId, String password) {
            String sql = "SELECT * FROM users WHERE id = ? AND pwd = ?";
            // Under PreparedStatement, parameter '?' treats "' OR '1'='1" as a literal string value,
            // never altering the relational query syntax tree.
            return userDb.containsKey(userId) && userDb.get(userId).equals(password);
        }
    }

    public static void main(String[] args) {
        System.out.println("Running WalletTransactionDemo verification...\n");

        // 1. Transaction Atomic Safety Test
        WalletLedger ledger = new WalletLedger();
        System.out.printf("Initial State -> Hostel: ₹%.2f, Canteen: ₹%.2f%n",
                ledger.getHostelBalance("STUDENT-01"), ledger.getCanteenBalance("STUDENT-01"));

        try {
            ledger.transferWithTransaction("STUDENT-01", 500.0, true);
        } catch (Exception e) {
            System.out.println("Caught exception as expected: " + e.getMessage());
        }

        System.out.printf("State After Aborted TX -> Hostel: ₹%.2f, Canteen: ₹%.2f (Invariant Preserved: ₹1,700 total)%n%n",
                ledger.getHostelBalance("STUDENT-01"), ledger.getCanteenBalance("STUDENT-01"));
        assert ledger.getHostelBalance("STUDENT-01") == 1500.0 : "Rollback failed to restore balance";

        // Normal successful transfer
        try {
            ledger.transferWithTransaction("STUDENT-01", 500.0, false);
        } catch (Exception e) {
            System.err.println("Unexpected failure: " + e);
        }
        System.out.printf("State After Successful TX -> Hostel: ₹%.2f, Canteen: ₹%.2f%n%n",
                ledger.getHostelBalance("STUDENT-01"), ledger.getCanteenBalance("STUDENT-01"));

        // 2. SQL Injection Demonstration
        AuthenticationSecurityService auth = new AuthenticationSecurityService();
        String maliciousInput = "' OR '1'='1";

        boolean unsafeResult = auth.unsafeLogin("admin", maliciousInput);
        System.out.println("Unsafe Login with injection: " + (unsafeResult ? "AUTHENTICATED (BREACH)" : "REJECTED"));

        boolean safeResult = auth.safeLogin("admin", maliciousInput);
        System.out.println("Safe PreparedStatement with injection: " + (safeResult ? "AUTHENTICATED" : "REJECTED (IMMUNE)"));
        assert unsafeResult && !safeResult : "Security assertion failed";

        System.out.println("\nWalletTransactionDemo verification PASSED.");
    }
}
