package task07;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * AdversarialSecurityTest
 * Exercises adversarial SQL injection vectors and maliciously corrupted
 * file formats against naive vs hardened parsing architectures.
 */
public class AdversarialSecurityTest {

    // =========================================================================
    // 1. ADVERSARIAL SQL INJECTION HARNESS
    // =========================================================================
    public static class SQLSecurityHarness {
        private final Map<String, String> credentialStore = new HashMap<>();

        public SQLSecurityHarness() {
            credentialStore.put("officer_admin", "StrictVaultPass2026!");
            credentialStore.put("clerk_sarah", "Passphrase#4321");
        }

        public boolean evaluateUnsafe(String user, String pwd) {
            String simulatedSql = "SELECT * FROM officers WHERE username='" + user + "' AND password='" + pwd + "'";
            System.out.println("  [UNSAFE ENGINE] Evaluating SQL: " + simulatedSql);

            // Parser interpretation:
            // If user passes: admin' OR '1'='1' -- or pwd passes ' OR '1'='1
            if (user.contains("' OR '1'='1") || pwd.contains("' OR '1'='1") ||
                user.contains("' OR 1=1") || pwd.contains("' OR 1=1")) {
                return true; // Tautology bypass
            }
            return credentialStore.containsKey(user) && credentialStore.get(user).equals(pwd);
        }

        public boolean evaluatePreparedStatement(String user, String pwd) {
            // PreparedStatement treats inputs strictly as literal values
            return credentialStore.containsKey(user) && credentialStore.get(user).equals(pwd);
        }

        public boolean evaluateManualEscape(String user, String pwd) {
            // Naive blacklisting / quote escaping attempt
            String sanitizedUser = user.replace("'", "\\'");
            String sanitizedPwd = pwd.replace("'", "\\'");
            String sql = "SELECT * FROM officers WHERE username='" + sanitizedUser + "' AND password='" + sanitizedPwd + "'";

            // Fragility: double escaping or second-order injection can still exploit this
            if (sanitizedPwd.contains("\\' OR \\'1\\'=\\'1")) {
                // Notice: In MySQL NO_BACKSLASH_ESCAPES mode, backslash is NOT an escape character!
                return true;
            }
            return credentialStore.containsKey(user) && credentialStore.get(user).equals(pwd);
        }
    }

    // =========================================================================
    // 2. CORRUPTED FILE STRESS HARNESS
    // =========================================================================
    public static class CorruptedFileHarness {
        public static class ParseResult {
            public int validCount = 0;
            public int corruptCount = 0;
            public final List<String> errorLog = new ArrayList<>();
        }

        public static ParseResult ingestCorruptedFile(Path path) throws IOException {
            ParseResult result = new ParseResult();
            try (BufferedReader br = Files.newBufferedReader(path)) {
                String line;
                int lineNum = 0;
                while ((line = br.readLine()) != null) {
                    lineNum++;
                    if (line.trim().isEmpty()) {
                        result.corruptCount++;
                        result.errorLog.add("Line " + lineNum + ": Empty line rejected.");
                        continue;
                    }
                    String[] tokens = line.split(",");
                    if (tokens.length != 5) {
                        result.corruptCount++;
                        result.errorLog.add("Line " + lineNum + ": Expected 5 fields, found " + tokens.length + " -> [" + line + "]");
                        continue;
                    }

                    try {
                        Integer.parseInt(tokens[3].trim());
                        Double.parseDouble(tokens[4].trim());
                        result.validCount++;
                    } catch (NumberFormatException e) {
                        result.corruptCount++;
                        result.errorLog.add("Line " + lineNum + ": Number format parse error on amount/route -> [" + line + "]");
                    }
                }
            }
            return result;
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=================================================");
        System.out.println("   ADVERSARIAL SECURITY & CORRUPTION AUDIT");
        System.out.println("=================================================");

        // Test 1: SQL Injection Vectors
        SQLSecurityHarness sqlHarness = new SQLSecurityHarness();
        String[] injectionVectors = {
                "' OR '1'='1",
                "officer_admin' OR '1'='1",
                "' OR 1=1 --"
        };

        for (String vector : injectionVectors) {
            boolean unsafeAccepted = sqlHarness.evaluateUnsafe("officer_admin", vector);
            boolean safeAccepted = sqlHarness.evaluatePreparedStatement("officer_admin", vector);
            System.out.printf("Payload: %-25s | Unsafe: %-10s | PreparedStatement: %-10s%n",
                    vector, (unsafeAccepted ? "BREACHED" : "DENIED"), (safeAccepted ? "BREACHED" : "IMMUNE"));
            assert unsafeAccepted && !safeAccepted : "SQL injection defense failed!";
        }

        // Test 2: Adversarial Corrupted File Formats
        System.out.println("\n--- Testing Corrupted Bus Pass File Ingestion ---");
        Path badFile = Files.createTempFile("adversarial_passes", ".csv");
        try (BufferedWriter bw = Files.newBufferedWriter(badFile)) {
            // 1. Valid record
            bw.write("P1001,192311001,Arun,12,4500.0\n");
            // 2. Corrupt: Empty line
            bw.write("\n");
            // 3. Corrupt: Missing fields
            bw.write("P1002,DAMAGED_RECORD_WITH_NO_AMOUNT\n");
            // 4. Corrupt: Non-numeric route
            bw.write("P1003,192311003,Divya,NOT_A_ROUTE,3800.0\n");
            // 5. Corrupt: Non-numeric amount
            bw.write("P1004,192311004,Karthik,8,FREE_PASS_NaN\n");
            // 6. Corrupt: Delimiter injection causing 7 fields
            bw.write("P1005,192311005,Malicious,Name,Injection,5,2500.0\n");
            // 7. Valid record
            bw.write("P1006,192311006,Suresh,14,5000.0\n");
        }

        CorruptedFileHarness.ParseResult result = CorruptedFileHarness.ingestCorruptedFile(badFile);
        System.out.printf("Total Valid Records Ingested:   %d%n", result.validCount);
        System.out.printf("Total Corrupted Lines Caught:   %d%n", result.corruptCount);
        System.out.println("Detailed Diagnostic Logs:");
        for (String err : result.errorLog) {
            System.out.println("  -> " + err);
        }

        assert result.validCount == 2 : "Must accept exactly 2 valid records";
        assert result.corruptCount == 5 : "Must catch all 5 corrupted inputs";

        Files.deleteIfExists(badFile);
        System.out.println("\nAdversarialSecurityTest verification PASSED.");
    }
}
