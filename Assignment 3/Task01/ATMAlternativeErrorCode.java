/**
 * Task 01: Alternative Concept & Complexity Analysis.
 * Compares Java Exception Handling vs. C-Style Error Return Codes.
 */
public class ATMAlternativeErrorCode {

    // Return codes
    public static final int SUCCESS = 0;
    public static final int ERR_CARD_NOT_FOUND = 1;
    public static final int ERR_INVALID_PIN = 2;
    public static final int ERR_INSUFFICIENT_FUNDS = 3;
    public static final int ERR_INVALID_AMOUNT = 4;

    private double balance = 15000.0;
    private final String validPin = "4829";

    // Returns integer error code instead of throwing exception
    public int withdrawWithCode(boolean cardPresent, String pin, double amount) {
        if (!cardPresent) return ERR_CARD_NOT_FOUND;
        if (!validPin.equals(pin)) return ERR_INVALID_PIN;
        if (amount <= 0) return ERR_INVALID_AMOUNT;
        if (amount > balance) return ERR_INSUFFICIENT_FUNDS;

        balance -= amount;
        return SUCCESS;
    }

    public static void main(String[] args) {
        ATMAlternativeErrorCode atm = new ATMAlternativeErrorCode();
        int iterations = 1_000_000;

        // 1. Benchmark Error Codes (Happy Path)
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            int code = atm.withdrawWithCode(true, "4829", 0.001);
            if (code != SUCCESS) {
                // handle error
            }
        }
        long codeTime = System.nanoTime() - start;

        System.out.println("==========================================================================");
        System.out.println("  ALTERNATIVE CONCEPT COMPARISON: EXCEPTIONS vs ERROR CODES               ");
        System.out.println("==========================================================================");
        System.out.printf("Error Codes Execution Time (%,d iterations): %,d ns%n", iterations, codeTime);
        System.out.println("\nArchitectural Comparison:");
        System.out.println("1. Error Codes: Extremely fast integer return, but callers can silently ignore errors.");
        System.out.println("2. Exceptions : Captures rich contextual stack traces and forces caller remediation,");
        System.out.println("   preventing corrupted transaction states in high-stakes financial operations.");
    }
}
