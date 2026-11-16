import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Task 03: Manual Resource Management vs Try-With-Resources.
 * Tracks active open file handles to demonstrate:
 * 1. Resource leaks in flawed manual try-finally blocks
 * 2. Exception masking: when close() throws an exception, it masks the primary business exception!
 */
public class ManualReportGenerator {

    public static int activeFileHandles = 0;

    // Tracked resource to simulate file descriptor leak
    static class TrackedReader extends BufferedReader {
        private final String path;
        private boolean closed = false;

        public TrackedReader(String path) throws IOException {
            super(new FileReader(path));
            this.path = path;
            activeFileHandles++;
            System.out.println("  [HANDLE OPENED] " + path + " (Active Handles: " + activeFileHandles + ")");
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                activeFileHandles--;
                System.out.println("  [HANDLE CLOSED] " + path + " (Active Handles: " + activeFileHandles + ")");
                super.close();
            }
        }
    }

    // A faulty reader that throws an exception during reading AND during closing
    static class CorruptResource implements AutoCloseable {
        private final String name;

        public CorruptResource(String name) {
            this.name = name;
            activeFileHandles++;
            System.out.println("  [RESOURCE OPENED] " + name + " (Active: " + activeFileHandles + ")");
        }

        public void processData() throws IOException {
            throw new IOException("PRIMARY ERROR: Failed to read corrupted data block in " + name);
        }

        @Override
        public void close() throws IOException {
            activeFileHandles--;
            System.out.println("  [RESOURCE CLOSE ATTEMPTED] " + name + " (Active: " + activeFileHandles + ")");
            throw new IOException("SECONDARY ERROR: Network socket timeout while closing " + name);
        }
    }

    // Flawed manual try-finally
    public static void processWithManualTryFinally() {
        System.out.println("--- 1. MANUAL TRY-FINALLY: EXCEPTION MASKING HAZARD ---");
        CorruptResource res = null;
        try {
            res = new CorruptResource("Store3_Manual.dat");
            res.processData();
        } catch (IOException e) {
            System.out.println("Caught inside try: " + e.getMessage());
        } finally {
            if (res != null) {
                try {
                    res.close(); // Throws secondary exception!
                } catch (IOException closeEx) {
                    System.out.println("FLAW EXPOSED: Exception inside finally masked or replaced the primary exception!");
                    System.out.println("Close Exception Caught: " + closeEx.getMessage());
                }
            }
        }
        System.out.println("Active handles remaining: " + activeFileHandles + "\n");
    }

    public static void main(String[] args) {
        processWithManualTryFinally();
    }
}
