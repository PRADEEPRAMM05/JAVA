import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Task 03: Robust Resource Safety via try-with-resources.
 * Demonstrates:
 * 1. Automatic resource closure guaranteed in reverse order of declaration
 * 2. Primary exception preservation with secondary close exceptions captured as SUPPRESSED exceptions
 * 3. Safe multi-file sales report consolidation
 */
public class TryWithResourcesReportGenerator {

    static class FaultyCloseable implements AutoCloseable {
        private final String resourceId;

        public FaultyCloseable(String resourceId) {
            this.resourceId = resourceId;
            System.out.println("  [TWR OPENED] " + resourceId);
        }

        public void readData() throws IOException {
            throw new IOException("PRIMARY FAILURE: Data checksum corruption in " + resourceId);
        }

        @Override
        public void close() throws IOException {
            System.out.println("  [TWR AUTO-CLOSING] " + resourceId);
            throw new IOException("SECONDARY ERROR: Stream flush failed during close() of " + resourceId);
        }
    }

    public static void testSuppressedExceptions() {
        System.out.println("--- 1. DEMONSTRATING SUPPRESSED EXCEPTIONS IN TRY-WITH-RESOURCES ---");
        try (FaultyCloseable res1 = new FaultyCloseable("Resource_A");
             FaultyCloseable res2 = new FaultyCloseable("Resource_B")) {

            res1.readData(); // Throws primary exception

        } catch (IOException primaryEx) {
            System.out.println("\n[PRIMARY EXCEPTION CAUGHT]");
            System.out.println("  Message: " + primaryEx.getMessage());

            Throwable[] suppressed = primaryEx.getSuppressed();
            System.out.println("\n[SUPPRESSED EXCEPTIONS PRESERVED: " + suppressed.length + " FOUND]");
            for (int i = 0; i < suppressed.length; i++) {
                System.out.printf("  Suppressed [%d]: %s%n", (i + 1), suppressed[i].getMessage());
            }
        }
        System.out.println();
    }

    public static void generateSalesReport(String[] storeFiles, String outputFile) {
        System.out.println("--- 2. CONSOLIDATING MULTI-STORE SALES REPORT SAFELY ---");
        double totalCompanyRevenue = 0.0;
        int transactionCount = 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("==================================================\n");
            writer.write("   CONSOLIDATED MULTI-STORE DAILY SALES REPORT    \n");
            writer.write("==================================================\n");

            for (String storeFile : storeFiles) {
                // Nested try-with-resources per store: an error in one file does not leak resources
                try (BufferedReader reader = new BufferedReader(new FileReader(storeFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            String item = parts[0].trim();
                            int qty = Integer.parseInt(parts[1].trim());
                            double price = Double.parseDouble(parts[2].trim());
                            double subtotal = qty * price;

                            totalCompanyRevenue += subtotal;
                            transactionCount++;
                            writer.write(String.format("Item: %-15s | Qty: %2d | Total: Rs. %,8.2f%n", item, qty, subtotal));
                        }
                    }
                    System.out.println("  Successfully processed store log: " + storeFile);
                } catch (IOException | NumberFormatException ex) {
                    System.out.println("  [WARNING] Corrupted or missing store file: " + storeFile + " (" + ex.getMessage() + ")");
                }
            }

            writer.write("--------------------------------------------------\n");
            writer.write(String.format("TOTAL TRANSACTIONS: %d%n", transactionCount));
            writer.write(String.format("TOTAL GROSS SALES : Rs. %,.2f%n", totalCompanyRevenue));
            writer.write("==================================================\n");

            System.out.printf("Summary report written to '%s' | Total Revenue: Rs. %,.2f%n", outputFile, totalCompanyRevenue);

        } catch (IOException e) {
            System.out.println("[CRITICAL] Could not write summary file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        testSuppressedExceptions();

        String[] sampleStores = {"store1.txt", "store2.txt", "store_missing.txt"};
        generateSalesReport(sampleStores, "summary_report.txt");
    }
}
