package task05;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Demonstrates the 4 Event-Handling Styles:
 * 1. Frame implements ActionListener
 * 2. Named Inner Class
 * 3. Anonymous Inner Class
 * 4. Lambda Expression / Method Reference
 */
public class ConverterFourStyles {

    private static double rate(String currency) {
        return currency.equals("USD") ? 0.012 : 0.011;
    }

    private static String compute(String text, String currency) {
        try {
            double val = Double.parseDouble(text.trim());
            return String.format("%.2f %s", val * rate(currency), currency);
        } catch (NumberFormatException e) {
            return "Invalid amount";
        }
    }

    // =========================================================================
    // STYLE A: Frame Itself Implements ActionListener
    // =========================================================================
    public static class StyleA_FrameAsListener extends JFrame implements ActionListener {
        private final JTextField amount = new JTextField(10);
        private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
        private final JLabel result = new JLabel(" ");
        private final JButton convertBtn = new JButton("Convert");

        public StyleA_FrameAsListener() {
            super("Style A: Frame as Listener");
            setLayout(new FlowLayout());
            add(new JLabel("INR")); add(amount); add(target); add(convertBtn); add(result);

            convertBtn.addActionListener(this);
            amount.addActionListener(this);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) { dispose(); }
            });
            pack();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            result.setText(compute(amount.getText(), (String) target.getSelectedItem()));
        }
    }

    // =========================================================================
    // STYLE B: Named Inner Class
    // =========================================================================
    public static class StyleB_NamedInnerClass extends JFrame {
        private final JTextField amount = new JTextField(10);
        private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
        private final JLabel result = new JLabel(" ");
        private final JButton convertBtn = new JButton("Convert");

        public StyleB_NamedInnerClass() {
            super("Style B: Named Inner Class");
            setLayout(new FlowLayout());
            add(new JLabel("INR")); add(amount); add(target); add(convertBtn); add(result);

            ConvertHandler handler = new ConvertHandler();
            convertBtn.addActionListener(handler);
            amount.addActionListener(handler);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) { dispose(); }
            });
            pack();
        }

        private class ConvertHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                result.setText(compute(amount.getText(), (String) target.getSelectedItem()));
            }
        }
    }

    // =========================================================================
    // STYLE C: Anonymous Inner Class
    // =========================================================================
    public static class StyleC_AnonymousInnerClass extends JFrame {
        private final JTextField amount = new JTextField(10);
        private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
        private final JLabel result = new JLabel(" ");
        private final JButton convertBtn = new JButton("Convert");

        public StyleC_AnonymousInnerClass() {
            super("Style C: Anonymous Inner Class");
            setLayout(new FlowLayout());
            add(new JLabel("INR")); add(amount); add(target); add(convertBtn); add(result);

            ActionListener handler = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    result.setText(compute(amount.getText(), (String) target.getSelectedItem()));
                }
            };
            convertBtn.addActionListener(handler);
            amount.addActionListener(handler);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) { dispose(); }
            });
            pack();
        }
    }

    // =========================================================================
    // STYLE D: Lambda Expression / Method Reference
    // =========================================================================
    public static class StyleD_Lambda extends JFrame {
        private final JTextField amount = new JTextField(10);
        private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
        private final JLabel result = new JLabel(" ");
        private final JButton convertBtn = new JButton("Convert");

        public StyleD_Lambda() {
            super("Style D: Lambda");
            setLayout(new FlowLayout());
            add(new JLabel("INR")); add(amount); add(target); add(convertBtn); add(result);

            ActionListener handler = e -> result.setText(compute(amount.getText(), (String) target.getSelectedItem()));
            convertBtn.addActionListener(handler);
            amount.addActionListener(handler);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) { dispose(); }
            });
            pack();
        }
    }

    public static void main(String[] args) {
        System.out.println("Instantiating all 4 styles to verify contracts...");
        new StyleA_FrameAsListener();
        new StyleB_NamedInnerClass();
        new StyleC_AnonymousInnerClass();
        new StyleD_Lambda();
        System.out.println("All 4 styles instantiated successfully.");
    }
}
