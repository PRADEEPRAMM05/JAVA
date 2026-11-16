import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Task 01 (Q1): Handling Failure in a Bank ATM Simulation.
 * Demonstrates:
 * 1. Checked Exceptions (InsufficientFundsException, FileNotFoundException)
 * 2. Unchecked Runtime Exceptions (InvalidPinException, NumberFormatException)
 * 3. Robust try-catch-finally control flow guaranteeing card ejection
 */

// Custom Checked Exception: recoverable domain condition requiring caller handling
class InsufficientFundsException extends Exception {
    private final double available;
    private final double requested;

    public InsufficientFundsException(double available, double requested) {
        super(String.format("Insufficient funds: Available Rs. %.2f, Requested Rs. %.2f", available, requested));
        this.available = available;
        this.requested = requested;
    }

    public double getDeficit() {
        return requested - available;
    }
}

// Custom Unchecked Exception: programming or authentication precondition failure
class InvalidPinException extends RuntimeException {
    public InvalidPinException(String message) {
        super(message);
    }
}

public class ATMSimulation {

    private double balance = 15000.0;
    private final String validPin = "4829";
    private boolean cardInserted = false;

    public void insertCard(String cardDataFilePath) throws FileNotFoundException {
        File file = new File(cardDataFilePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Card data file not found: " + cardDataFilePath);
        }
        cardInserted = true;
        System.out.println("[ATM] Card read successfully from " + cardDataFilePath);
    }

    public void authenticate(String pin) {
        if (!cardInserted) {
            throw new IllegalStateException("No card inserted in ATM slot");
        }
        if (pin == null || pin.length() != 4 || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must be exactly 4 numeric digits");
        }
        if (!pin.equals(validPin)) {
            throw new InvalidPinException("Authentication Failed: Incorrect PIN entered.");
        }
        System.out.println("[ATM] PIN verified. Welcome, Account Holder.");
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (!cardInserted) {
            throw new IllegalStateException("No card inserted in ATM slot");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be strictly positive: " + amount);
        }
        if (amount > balance) {
            throw new InsufficientFundsException(balance, amount);
        }
        balance -= amount;
        System.out.printf("[ATM] Dispensing Cash: Rs. %.2f | New Balance: Rs. %.2f%n", amount, balance);
    }

    public void ejectCard() {
        if (cardInserted) {
            cardInserted = false;
            System.out.println("[ATM-FINALLY] Card ejected safely. Session closed.\n");
        }
    }

    public static void runCustomerScenario(String description, String cardFile, String pin, String amountStr) {
        System.out.println(">>> Scenario: " + description);
        ATMSimulation atm = new ATMSimulation();

        try {
            // Step 1: Read card
            atm.insertCard(cardFile);

            // Step 2: Authenticate
            atm.authenticate(pin);

            // Step 3: Parse amount and withdraw
            double amount = Double.parseDouble(amountStr);
            atm.withdraw(amount);

        } catch (FileNotFoundException e) {
            System.out.println("[CHECKED CAUGHT] " + e.getMessage());
        } catch (InvalidPinException e) {
            System.out.println("[UNCHECKED CAUGHT] " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.printf("[CHECKED CAUGHT] %s (Deficit: Rs. %.2f)%n", e.getMessage(), e.getDeficit());
        } catch (NumberFormatException e) {
            System.out.println("[INPUT FORMAT ERROR] User entered non-numeric amount: '" + amountStr + "'");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[PRECONDITION ERROR] " + e.getMessage());
        } finally {
            // ALWAYS GUARANTEED TO RUN: Prevents card trapping inside ATM
            atm.ejectCard();
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("    TASK 01: BANK ATM EXCEPTION HANDLING & RESOURCE RECOVERY SUITE        ");
        System.out.println("==========================================================================\n");

        // Scenario 1: Normal successful withdrawal
        runCustomerScenario("1. Normal Withdrawal", "card_valid.txt", "4829", "5000");

        // Scenario 2: Unchecked Exception - Wrong PIN
        runCustomerScenario("2. Invalid PIN Security Failure", "card_valid.txt", "0000", "5000");

        // Scenario 3: Unchecked Exception - Alphanumeric parsing failure
        runCustomerScenario("3. Non-numeric Input Typed", "card_valid.txt", "4829", "five_thousand");

        // Scenario 4: Checked Exception - Insufficient Funds
        runCustomerScenario("4. Overdraft Exceeding Balance", "card_valid.txt", "4829", "50000");

        // Scenario 5: Checked Exception - Missing Hardware File
        runCustomerScenario("5. Missing Card Reader Hardware File", "missing_reader.dat", "4829", "1000");
    }
}
