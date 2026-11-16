import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Task 01: GUI Design Review - Diagnose and Repair Poor Event-Handling Design.
 * 
 * Flaws in original design:
 * 1. "God Listener" anti-pattern: Frame implements ActionListener with a massive if-else chain.
 * 2. Tight coupling: UI components, validation, payment processing, and receipt generation all mixed together.
 * 3. Fragile component comparison: e.getSource() == button breaks when buttons are replaced or refactored.
 * 
 * Repaired design:
 * 1. Decoupled Service Layer (FeePaymentService).
 * 2. Focused, single-purpose lambda action listeners attached directly to each control.
 * 3. Clean validation and state feedback.
 */
public class RepairedFeeWindow extends Frame {

    // Domain Service decoupling UI from business operations
    static class FeePaymentService {
        public static boolean processPayment(String regNo, double amount) {
            // Simulated payment gateway verification
            return regNo != null && !regNo.trim().isEmpty() && amount > 0;
        }

        public static String generateReceipt(String regNo, double amount) {
            return "RECEIPT #" + System.currentTimeMillis() % 100000 + " | Reg: " + regNo + " | Paid: Rs. " + amount;
        }
    }

    private final TextField regNoField = new TextField(15);
    private final TextField amountField = new TextField(10);
    private final Label statusLabel = new Label("Ready for student fee entry.");

    public RepairedFeeWindow() {
        super("Student Fee Payment System (Refactored)");
        setLayout(new BorderLayout(10, 10));

        // Form Input Panel
        Panel formPanel = new Panel(new GridLayout(2, 2, 5, 5));
        formPanel.add(new Label("Student Register No:"));
        formPanel.add(regNoField);
        formPanel.add(new Label("Fee Amount (Rs):"));
        formPanel.add(amountField);
        add(formPanel, BorderLayout.NORTH);

        // Status Feedback in Center
        add(statusLabel, BorderLayout.CENTER);

        // Button Controls Panel
        Panel buttonPanel = new Panel(new FlowLayout());
        Button payButton = new Button("Pay Fee");
        Button receiptButton = new Button("Print Receipt");
        Button clearButton = new Button("Clear");

        buttonPanel.add(payButton);
        buttonPanel.add(receiptButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- REPAIRED EVENT LISTENERS: DIRECT, FOCUSED LAMBDAS ---
        payButton.addActionListener(e -> handlePayment());
        receiptButton.addActionListener(e -> handleReceipt());
        clearButton.addActionListener(e -> handleClear());

        // Window Closing Listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setSize(420, 200);
        setLocationRelativeTo(null);
    }

    private void handlePayment() {
        String reg = regNoField.getText().trim();
        try {
            double amt = Double.parseDouble(amountField.getText().trim());
            if (FeePaymentService.processPayment(reg, amt)) {
                statusLabel.setText("Payment Successful! Rs. " + amt + " received for " + reg);
            } else {
                statusLabel.setText("Payment Rejected: Enter valid student and amount.");
            }
        } catch (NumberFormatException ex) {
            statusLabel.setText("Input Error: Fee amount must be a valid number.");
        }
    }

    private void handleReceipt() {
        String reg = regNoField.getText().trim();
        try {
            double amt = Double.parseDouble(amountField.getText().trim());
            String receipt = FeePaymentService.generateReceipt(reg, amt);
            statusLabel.setText(receipt);
        } catch (NumberFormatException ex) {
            statusLabel.setText("Cannot print receipt: Invalid fee amount.");
        }
    }

    private void handleClear() {
        regNoField.setText("");
        amountField.setText("");
        statusLabel.setText("Form cleared. Ready for next entry.");
    }

    public static void main(String[] args) {
        // Run in headless/console safe manner for verification
        RepairedFeeWindow win = new RepairedFeeWindow();
        System.out.println("RepairedFeeWindow initialized successfully with decoupled listeners.");
        win.handleClear();
        System.out.println("Status: " + win.statusLabel.getText());
        win.regNoField.setText("192524377");
        win.amountField.setText("15000");
        win.handlePayment();
        System.out.println("Status: " + win.statusLabel.getText());
        win.handleReceipt();
        System.out.println("Status: " + win.statusLabel.getText());
        win.dispose();
    }
}
