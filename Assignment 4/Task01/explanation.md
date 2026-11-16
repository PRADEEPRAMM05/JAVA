# Assignment 4 – Task 01: GUI Design Review & Refactoring

## 1. Diagnosis of the Poor Event-Handling Design

In the junior developer's original fee payment window:
```java
public class FeeWindow extends Frame implements ActionListener {
    TextField regNo = new TextField();
    TextField amount = new TextField();
    Button pay = new Button("Pay");
    Button receipt = new Button("Print Receipt");
    Button clear = new Button("Clear");

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == pay) { ... }
        else if (e.getSource() == receipt) { ... }
        else if (e.getSource() == clear) { ... }
    }
}
```

### Architectural Defects Identified:
1. **The "God Listener" Anti-Pattern**:
   The `Frame` class implements `ActionListener` itself. All button clicks from across the entire interface funnel into a single monolithic `actionPerformed()` method containing an ever-growing `if-else` cascade.
2. **Violation of Single Responsibility Principle (SRP)**:
   The GUI window class simultaneously manages component layout, input parsing, bank transaction processing, receipt generation, and error presentation.
3. **Fragile Component Comparison (`e.getSource() == pay`)**:
   Comparing component references directly prevents dynamic UI refactoring, internationalization, or sharing action handlers across menu items, toolbars, and keyboard shortcuts.
4. **EDT Blocking Hazard**:
   Executing payment processing or receipt printing directly inside `actionPerformed()` runs on the single **Event Dispatch Thread (EDT)**. If network or printer latency occurs, the entire GUI freezes, buttons stop repainting, and the OS marks the app as "Not Responding".

---

## 2. Refactored Solution Architecture (`RepairedFeeWindow.java`)

1. **Separation of Concerns via Service Layer**:
   Extracted `FeePaymentService` to encapsulate payment validation and receipt generation. The UI merely collects inputs and presents results.
2. **Decoupled Lambda Listeners**:
   Each button is configured with its own focused lambda action listener attached directly via `addActionListener(e -> ...)`:
   ```java
   payButton.addActionListener(e -> handlePayment());
   receiptButton.addActionListener(e -> handleReceipt());
   clearButton.addActionListener(e -> handleClear());
   ```
3. **Defensive Validation & Clear Status Feedback**:
   Input values are validated and formatted, presenting clear diagnostic messages without crashing.

---

## 3. Verification & Execution Output

```text
RepairedFeeWindow initialized successfully with decoupled listeners.
Status: Form cleared. Ready for next entry.
Status: Payment Successful! Rs. 15000.0 received for 192524377
Status: RECEIPT #95670 | Reg: 192524377 | Paid: Rs. 15000.0
```
