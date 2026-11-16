package task05;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Converter - Completed Implementation (Style D: Lambda Listener)
 * Fulfills TODO 1 and TODO 2 from ASSIGNMENT 4 Task 5.
 */
public class Converter extends JFrame {

    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");

    private static double rate(String currency) {
        return currency.equals("USD") ? 0.012 : 0.011;
    }

    public Converter() {
        super("Currency Converter");
        setLayout(new FlowLayout());

        JButton convertButton = new JButton("Convert");
        add(new JLabel("INR"));
        add(amount);
        add(target);
        add(convertButton);
        add(result);

        // TODO 1: Register ONE lambda listener for both the Convert button and the
        // Enter key in the amount field
        ActionListener conversionListener = e -> convert();
        convertButton.addActionListener(conversionListener);
        amount.addActionListener(conversionListener);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void convert() {
        // TODO 2: Parse the amount and show the converted value with 2 decimals, or
        // "Invalid amount" if parsing fails
        try {
            String text = amount.getText().trim();
            double val = Double.parseDouble(text);
            if (val < 0) {
                result.setText("Invalid amount");
                return;
            }
            String selectedCurrency = (String) target.getSelectedItem();
            double converted = val * rate(selectedCurrency);
            result.setText(String.format("%.2f %s", converted, selectedCurrency));
        } catch (NumberFormatException ex) {
            result.setText("Invalid amount");
        }
    }

    // Testing helper getters
    public void setAmountText(String text) { amount.setText(text); }
    public void setTargetCurrency(String curr) { target.setSelectedItem(curr); }
    public String getResultText() { return result.getText(); }
    public void triggerConversion() { convert(); }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--test")) {
            System.out.println("Running Converter automated test suite...");
            Converter c = new Converter();

            // Test 1: Valid USD
            c.setAmountText("1000");
            c.setTargetCurrency("USD");
            c.triggerConversion();
            System.out.println("Test 1 (1000 INR -> USD): " + c.getResultText());
            assert "12.00 USD".equals(c.getResultText()) : "USD conversion failed";

            // Test 2: Valid EUR
            c.setAmountText("1000");
            c.setTargetCurrency("EUR");
            c.triggerConversion();
            System.out.println("Test 2 (1000 INR -> EUR): " + c.getResultText());
            assert "11.00 EUR".equals(c.getResultText()) : "EUR conversion failed";

            // Test 3: Invalid Input
            c.setAmountText("abc");
            c.triggerConversion();
            System.out.println("Test 3 (Invalid 'abc'): " + c.getResultText());
            assert "Invalid amount".equals(c.getResultText()) : "Invalid input check failed";

            System.out.println("Converter verification PASSED.");
            System.exit(0);
        }

        SwingUtilities.invokeLater(() -> new Converter().setVisible(true));
    }
}
