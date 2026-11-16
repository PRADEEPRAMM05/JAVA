# Task 02: From Requirement to Event-Driven Design (Hospital Outpatient Appointment Desk)

## 1. Event Model Analysis (Task 2.1)

The table below catalogs every UI event source, event object generated, required listener interface, and resulting system action:

| Event Source | Event Object | Listener Interface | Triggering Action | Handler Action Taken |
| :--- | :--- | :--- | :--- | :--- |
| `departmentCombo` (`JComboBox`) | `ActionEvent` / `ItemEvent` | `ActionListener` / `ItemListener` | User selects a different department | Clears and repopulates `doctorCombo` with doctors specializing in the selected department. |
| `bookButton` (`JButton`) | `ActionEvent` | `ActionListener` | User clicks "Book Appointment" | Validates patient ID and inputs; prompts modal confirmation dialog; persists appointment record. |
| `clearButton` (`JButton`) | `ActionEvent` | `ActionListener` | User clicks "Clear Form" | Clears text fields, resets department to default, resets status banner. |
| `this` (`AppointmentDesk` / `JFrame`) | `WindowEvent` | `WindowListener` (`WindowAdapter`) | User clicks window close button ('X') | Checks if `isBookingInProgress()` is true; if so, displays confirmation dialog preventing accidental data loss before disposing. |

---

## 2. Component Containment Hierarchy (Task 2.2)

```text
JFrame: AppointmentDesk (BorderLayout)
│
├── [NORTH] JLabel: statusLabel (Etched border status banner)
│
├── [CENTER] JPanel: formPanel (GridBagLayout, TitledBorder)
│   ├── (0, 0) JLabel: "Patient ID:"
│   ├── (1, 0) JTextField: patientIdField
│   ├── (0, 1) JLabel: "Patient Name:"
│   ├── (1, 1) JTextField: patientNameField
│   ├── (0, 2) JLabel: "Department:"
│   ├── (1, 2) JComboBox<String>: departmentCombo
│   ├── (0, 3) JLabel: "Consultant Doctor:"
│   ├── (1, 3) JComboBox<String>: doctorCombo
│   ├── (0, 4) JLabel: "Available Slot:"
│   └── (1, 4) JComboBox<String>: slotCombo
│
└── [SOUTH] JPanel: buttonPanel (FlowLayout.RIGHT)
    ├── JButton: clearButton ("Clear Form")
    └── JButton: bookButton ("Book Appointment")
```

---

## 3. Handler Logic Pseudocode (Task 2.3)

### (a) Refreshing Doctor List upon Department Change
```text
ALGORITHM RefreshDoctorList(selectedDepartment)
INPUT: selectedDepartment as String
OUTPUT: Updated doctorCombo dropdown

BEGIN
    doctorCombo.removeAllItems()
    IF DEPT_DOCTOR_MAP contains key selectedDepartment THEN
        FOR EACH doctor IN DEPT_DOCTOR_MAP.get(selectedDepartment) DO
            doctorCombo.addItem(doctor)
        END FOR
    END IF
END
```

### (b) Validating and Booking Outpatient Appointment
```text
ALGORITHM HandleBooking()
INPUT: User inputs from patientIdField, patientNameField, departmentCombo, doctorCombo, slotCombo
OUTPUT: Boolean success, confirmation dialog, and appointment booking

BEGIN
    patientId ← TRIM(patientIdField.getText())
    patientName ← TRIM(patientNameField.getText())
    department ← departmentCombo.getSelectedItem()
    doctor ← doctorCombo.getSelectedItem()
    slot ← slotCombo.getSelectedItem()

    IF patientId IS EMPTY OR NOT MATCHES("^[A-Za-z0-9-]+$") THEN
        SHOW_ERROR("Invalid Patient ID: Must be alphanumeric.")
        patientIdField.requestFocus()
        RETURN FALSE
    END IF

    IF patientName IS EMPTY THEN
        SHOW_ERROR("Patient Name cannot be blank.")
        patientNameField.requestFocus()
        RETURN FALSE
    END IF

    message ← FORMAT("Confirm Appointment for {patientId} ({patientName}) with {doctor} at {slot}?")
    choice ← SHOW_CONFIRM_DIALOG(message, "Confirm Outpatient Appointment")

    IF choice == YES_OPTION THEN
        record ← FORMAT("{patientId} | {doctor} | {slot} | {department}")
        bookedAppointments.add(record)
        statusLabel.setText("Successfully booked appointment for " + patientId)
        SHOW_INFO_DIALOG("Appointment Confirmed!", "Success")
        RESET_FORM()
        RETURN TRUE
    ELSE
        statusLabel.setText("Booking cancelled by user.")
        RETURN FALSE
    END IF
END
```

---

## 4. Event Sequence Trace & Applet Lifecycle Comparison (Task 2.4)

### Execution Trace:
1. **Window Construction & Opening:**
   - `main()` invokes `SwingUtilities.invokeLater(...)`.
   - `AppointmentDesk()` constructor executes: sets layout, instantiates components, attaches event listeners.
   - Initial call to `refreshDoctorList()` loads default department doctors.
   - `setVisible(true)` triggers OS window manager peering and dispatches `windowOpened(WindowEvent)`.
2. **Department Selection:**
   - User clicks dropdown item: OS mouse event -> AWT Event Queue -> `ItemEvent` and `ActionEvent` dispatched on EDT.
   - `refreshDoctorList()` executes, rebuilding the dependent combo model synchronously.
3. **Appointment Booking:**
   - User clicks "Book": `ActionEvent` dispatched to `handleBooking()`.
   - Input validation -> modal `JOptionPane` blocks parent interaction until user selection.
4. **Window Closing:**
   - User clicks close box: `windowClosing(WindowEvent)` intercepted by `WindowAdapter`.
   - Condition `isBookingInProgress()` evaluated; if dirty, exit confirmation dialog appears.
   - Upon confirmation, `dispose()` is invoked -> `windowClosed(WindowEvent)` -> process terminates.

### Comparison with Applet Lifecycle:
| AWT/Swing Desktop Lifecycle | Classic Applet Lifecycle | Semantic Difference |
| :--- | :--- | :--- |
| Constructor / `init()`-equivalent | `init()` | Desktop initializes peers and layouts; Applet prepares browser sandbox. |
| `setVisible(true)` / `windowOpened()` | `start()` | Desktop maps window to OS display buffer; Applet starts rendering and background threads whenever scrolled into view. |
| User interaction events on EDT | Repaint / event dispatching | Both use AWT event delegation model. |
| Minimize / unfocus | `stop()` | Desktop window retains state in background; Applet pauses animations and active threads to save browser CPU. |
| `windowClosing()` / `dispose()` | `destroy()` | Desktop tears down native OS window handle; Applet releases browser runtime resources and unloads. |

---

## 5. Alternative Concept & Complexity Analysis

### Comparison: In-Memory Map vs. Dynamic Database Fetch per Department Change

```text
+------------------------------+------------------------------------+------------------------------------+
| Metric                       | In-Memory Map Cache                | Dynamic JDBC Query per Change      |
+------------------------------+------------------------------------+------------------------------------+
| Latency per Combo Selection  | O(1) hash lookup (~5 microseconds) | 15 - 80 ms (Network + DB SQL query)|
| DB Connection Overhead       | 1 connection at boot (or static)   | Connection pool fetch per click    |
| Offline / Network Resilience | 100% immune to connection drops    | Fails if DB socket times out       |
| Data Freshness               | Snapshot at application launch     | Real-time doctor schedule updates  |
| Memory Footprint             | O(d * m) for d departments,        | O(m) temporary query buffer        |
|                              | m doctors per department (~20 KB)  | (~2 KB)                            |
+------------------------------+------------------------------------+------------------------------------+
```

**Recommendation:** For standard outpatient clinics where doctor affiliations rarely change intra-day, an **In-Memory Catalog with Scheduled Invalidation** delivers instant zero-lag UI response on the Event-Dispatch Thread while completely avoiding UI freezes.
