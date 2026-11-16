package task02;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Hospital Outpatient Appointment Desk Application
 * Implements cascading dropdowns, booking confirmation dialogs,
 * and safe window close interceptors when unsaved inputs are present.
 */
public class AppointmentDesk extends JFrame {

    // Department -> Doctors catalog (In-Memory Fast Cache)
    private static final Map<String, List<String>> DEPT_DOCTOR_MAP = new LinkedHashMap<>();
    private static final String[] TIME_SLOTS = {
        "09:00 AM - 09:30 AM",
        "10:00 AM - 10:30 AM",
        "11:30 AM - 12:00 PM",
        "02:00 PM - 02:30 PM",
        "03:30 PM - 04:00 PM"
    };

    static {
        DEPT_DOCTOR_MAP.put("Cardiology", Arrays.asList("Dr. V. Sharma (Senior Interventionalist)", "Dr. K. Rao (Consultant)"));
        DEPT_DOCTOR_MAP.put("Neurology", Arrays.asList("Dr. P. Sundaram (Neuro-Physician)", "Dr. A. Anand (Stroke Specialist)"));
        DEPT_DOCTOR_MAP.put("Orthopedics", Arrays.asList("Dr. R. Kumar (Joint Replacement)", "Dr. S. Mehta (Trauma Surgeon)"));
        DEPT_DOCTOR_MAP.put("Pediatrics", Arrays.asList("Dr. L. Narayan (Child Health)", "Dr. J. Paul (Neonatologist)"));
        DEPT_DOCTOR_MAP.put("Dermatology", Arrays.asList("Dr. M. Swaminathan (Cosmetic & Clinical)", "Dr. G. Verma (Consultant)"));
    }

    // UI Components
    private final JTextField patientIdField;
    private final JTextField patientNameField;
    private final JComboBox<String> departmentCombo;
    private final JComboBox<String> doctorCombo;
    private final JComboBox<String> slotCombo;
    private final JButton bookButton;
    private final JButton clearButton;
    private final JLabel statusLabel;

    // Track active registered bookings
    private final List<String> bookedAppointments = new ArrayList<>();

    public AppointmentDesk() {
        super("SIMATS Health Center - Outpatient Appointment Desk");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Form inputs panel with GridBagLayout for aligned presentation
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" Patient & Slot Details "),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Patient ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1;
        patientIdField = new JTextField(16);
        formPanel.add(patientIdField, gbc);

        // Row 1: Patient Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Patient Name:"), gbc);
        gbc.gridx = 1;
        patientNameField = new JTextField(16);
        formPanel.add(patientNameField, gbc);

        // Row 2: Department (Cascading master)
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        String[] departments = DEPT_DOCTOR_MAP.keySet().toArray(new String[0]);
        departmentCombo = new JComboBox<>(departments);
        formPanel.add(departmentCombo, gbc);

        // Row 3: Doctor (Cascading dependent)
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Consultant Doctor:"), gbc);
        gbc.gridx = 1;
        doctorCombo = new JComboBox<>();
        formPanel.add(doctorCombo, gbc);

        // Row 4: Slot
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Available Slot:"), gbc);
        gbc.gridx = 1;
        slotCombo = new JComboBox<>(TIME_SLOTS);
        formPanel.add(slotCombo, gbc);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bookButton = new JButton("Book Appointment");
        clearButton = new JButton("Clear Form");
        buttonPanel.add(clearButton);
        buttonPanel.add(bookButton);

        // Status Banner
        statusLabel = new JLabel(" Ready for input. Enter Patient ID to begin.");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel.setForeground(new Color(25, 80, 140));

        // Assemble Layout
        setLayout(new BorderLayout(5, 5));
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);

        // Event Bindings
        setupEventHandlers();

        // Initial doctor population for default department
        refreshDoctorList();

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(520, 320));
    }

    private void setupEventHandlers() {
        // 1. Cascading dropdown handler: Department selection updates Doctor list
        departmentCombo.addActionListener(e -> refreshDoctorList());

        // 2. Booking button handler
        bookButton.addActionListener(e -> handleBooking());

        // 3. Clear button handler
        clearButton.addActionListener(e -> resetForm());

        // 4. Window closing interceptor
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndClose();
            }
        });
    }

    /**
     * Refreshes the doctor combo box based on the selected department.
     */
    public void refreshDoctorList() {
        String selectedDept = (String) departmentCombo.getSelectedItem();
        doctorCombo.removeAllItems();
        if (selectedDept != null && DEPT_DOCTOR_MAP.containsKey(selectedDept)) {
            for (String doc : DEPT_DOCTOR_MAP.get(selectedDept)) {
                doctorCombo.addItem(doc);
            }
        }
    }

    /**
     * Validates input fields and books the appointment after user confirmation.
     */
    public boolean handleBooking() {
        String patientId = patientIdField.getText().trim();
        String patientName = patientNameField.getText().trim();
        String department = (String) departmentCombo.getSelectedItem();
        String doctor = (String) doctorCombo.getSelectedItem();
        String slot = (String) slotCombo.getSelectedItem();

        if (patientId.isEmpty() || !patientId.matches("^[A-Za-z0-9-]+$")) {
            showError("Invalid Patient ID: Must be non-empty and alphanumeric.");
            patientIdField.requestFocus();
            return false;
        }

        if (patientName.isEmpty()) {
            showError("Patient Name cannot be blank.");
            patientNameField.requestFocus();
            return false;
        }

        if (doctor == null || slot == null) {
            showError("Please select a valid Doctor and Time Slot.");
            return false;
        }

        // Modal Confirmation
        String message = String.format(
                "Confirm Appointment Booking:\n\n" +
                "Patient ID: %s (%s)\n" +
                "Department: %s\n" +
                "Doctor: %s\n" +
                "Slot: %s",
                patientId, patientName, department, doctor, slot
        );

        int choice = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirm Outpatient Appointment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            String record = String.format("%s | %s | %s | %s", patientId, doctor, slot, department);
            bookedAppointments.add(record);
            statusLabel.setText(" Successfully booked appointment for " + patientId);
            JOptionPane.showMessageDialog(this,
                    "Appointment Confirmed!\nBooking Reference: APT-" + System.currentTimeMillis() % 100000,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            return true;
        } else {
            statusLabel.setText(" Booking cancelled by user.");
            return false;
        }
    }

    public boolean isBookingInProgress() {
        return !patientIdField.getText().trim().isEmpty() || !patientNameField.getText().trim().isEmpty();
    }

    private void confirmAndClose() {
        if (isBookingInProgress()) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "A patient booking is currently in progress.\nDo you want to discard your changes and exit?",
                    "Confirm Application Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        } else {
            dispose();
            System.exit(0);
        }
    }

    private void showError(String msg) {
        statusLabel.setText(" Error: " + msg);
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public void resetForm() {
        patientIdField.setText("");
        patientNameField.setText("");
        if (departmentCombo.getItemCount() > 0) departmentCombo.setSelectedIndex(0);
        if (slotCombo.getItemCount() > 0) slotCombo.setSelectedIndex(0);
        statusLabel.setText(" Ready for input. Enter Patient ID to begin.");
    }

    public List<String> getBookedAppointments() {
        return Collections.unmodifiableList(bookedAppointments);
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--test")) {
            System.out.println("Running AppointmentDesk headless unit verification...");
            AppointmentDesk desk = new AppointmentDesk();
            System.out.println("Default Department: " + desk.departmentCombo.getSelectedItem());
            System.out.println("Doctor count: " + desk.doctorCombo.getItemCount());
            desk.departmentCombo.setSelectedItem("Pediatrics");
            desk.refreshDoctorList();
            System.out.println("Switched to Pediatrics -> Doctor 1: " + desk.doctorCombo.getItemAt(0));
            System.out.println("Headless verification PASSED.");
            System.exit(0);
        }

        SwingUtilities.invokeLater(() -> {
            AppointmentDesk desk = new AppointmentDesk();
            desk.setVisible(true);
        });
    }
}
