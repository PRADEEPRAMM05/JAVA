import java.io.IOException;

/**
 * Task 02: Multi-Tiered Exception Propagation and Chaining in Examination Portal.
 * Architecture:
 * - Layer 3 (Data/Storage): Throws low-level IOException or SQLException
 * - Layer 2 (Service Layer): Wraps into domain-specific SubmissionStorageException or SessionExpiredException
 * - Layer 1 (UI/Controller): Catches high-level ExamException, displays friendly message, and audits root cause
 */
public class ExamPortalPropagation {

    // Domain Exception Hierarchy
    static class ExamException extends Exception {
        ExamException(String message) { super(message); }
        ExamException(String message, Throwable cause) { super(message, cause); }
    }

    static class SessionExpiredException extends ExamException {
        SessionExpiredException(String message) { super(message); }
    }

    static class NetworkTimeoutException extends ExamException {
        NetworkTimeoutException(String message, Throwable cause) { super(message, cause); }
    }

    static class ExamSubmissionException extends ExamException {
        ExamSubmissionException(String message, Throwable cause) { super(message, cause); }
    }

    // Layer 3: Storage Driver
    static class StorageDriver {
        static void writeToDisk(String studentId, byte[] pdfData) throws IOException {
            if (pdfData.length > 5_000_000) {
                throw new IOException("Remote storage server disk write failed: ErrorCode 507 - Insufficient Storage");
            }
            System.out.println("  [StorageDriver] Wrote " + pdfData.length + " bytes for " + studentId);
        }
    }

    // Layer 2: Examination Service Layer
    static class ExamService {
        static void submitAnswerSheet(String studentId, boolean sessionActive, byte[] pdfData) throws ExamException {
            if (!sessionActive) {
                throw new SessionExpiredException("Exam session timed out for student " + studentId);
            }

            try {
                StorageDriver.writeToDisk(studentId, pdfData);
            } catch (IOException ioEx) {
                // EXCEPTION CHAINING: Wrapping lower technical exception into meaningful business domain exception
                throw new ExamSubmissionException("Unable to process exam submission for " + studentId, ioEx);
            }
        }
    }

    // Layer 1: UI / Web Controller Layer
    public static void handleSubmissionRequest(String studentId, boolean sessionActive, int fileSize) {
        System.out.printf("--- Processing Submission: %s (Size: %,d bytes, Active: %b) ---%n",
                studentId, fileSize, sessionActive);
        byte[] payload = new byte[fileSize];

        try {
            ExamService.submitAnswerSheet(studentId, sessionActive, payload);
            System.out.println("  [UI SUCCESS] Answer sheet accepted and timestamped.\n");
        } catch (SessionExpiredException e) {
            System.out.println("  [UI PROMPT] Session Expired: Please re-authenticate and contact proctor.");
            System.out.println("  [AUDIT LOG] " + e.getMessage() + "\n");
        } catch (ExamSubmissionException e) {
            System.out.println("  [UI ERROR] Submission Failed: " + e.getMessage());
            System.out.println("  [ROOT CAUSE DETECTED] Class: " + e.getCause().getClass().getName());
            System.out.println("  [ROOT CAUSE DETAILS]  " + e.getCause().getMessage() + "\n");
        } catch (ExamException e) {
            System.out.println("  [UI GENERIC ERROR] Unexpected Exam Error: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println(" TASK 02: EXCEPTION PROPAGATION & CAUSALITY CHAINING IN EXAM PORTAL       ");
        System.out.println("==========================================================================\n");

        // Scenario 1: Normal valid submission
        handleSubmissionRequest("STU-101", true, 250_000);

        // Scenario 2: Session timeout exception (direct propagation)
        handleSubmissionRequest("STU-102", false, 150_000);

        // Scenario 3: Storage failure wrapped via exception chaining
        handleSubmissionRequest("STU-103", true, 8_000_000);
    }
}
