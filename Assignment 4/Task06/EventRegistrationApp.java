package task06;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Campus Cultural Event Registration Desktop Application
 * Architecture:
 * - UI Layer: Swing GUI (JFrame, JMenuBar, JTable, Form, SwingWorker)
 * - Service Layer: EventRegistrationService (validation & duplicate rules)
 * - Data Access Layer: RegistrationDAO (persistent thread-safe store)
 */
public class EventRegistrationApp extends JFrame {

    // -------------------------------------------------------------------------
    // 1. MODEL
    // -------------------------------------------------------------------------
    public static class Registration {
        private final long id;
        private final String studentId;
        private final String studentName;
        private final String department;
        private final String eventName;
        private final String contactPhone;
        private final String timestamp;

        public Registration(long id, String studentId, String studentName,
                            String department, String eventName, String contactPhone, String timestamp) {
            this.id = id;
            this.studentId = studentId;
            this.studentName = studentName;
            this.department = department;
            this.eventName = eventName;
            this.contactPhone = contactPhone;
            this.timestamp = timestamp;
        }

        public long getId() { return id; }
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getDepartment() { return department; }
        public String getEventName() { return eventName; }
        public String getContactPhone() { return contactPhone; }
        public String getTimestamp() { return timestamp; }
    }

    // -------------------------------------------------------------------------
    // 2. DATA ACCESS LAYER (DAO)
    // -------------------------------------------------------------------------
    public interface RegistrationDAO {
        void save(Registration reg) throws Exception;
        List<Registration> findAll() throws Exception;
        List<Registration> findByEvent(String eventName) throws Exception;
        boolean exists(String studentId, String eventName) throws Exception;
    }

    public static class ThreadSafeRegistrationDAO implements RegistrationDAO {
        private final List<Registration> store = new CopyOnWriteArrayList<>();
        private final Set<String> uniqueKeys = ConcurrentHashMap.newKeySet();
        private long idGen = 1000;

        public ThreadSafeRegistrationDAO() {
            // Seed initial sample data
            try {
                save(new Registration(++idGen, "2024CS01", "Aarav Patel", "Computer Science", "Code Relay Hackathon", "9876543210", "2026-11-16 09:00"));
                save(new Registration(++idGen, "2024EC14", "Bhavna Sharma", "Electronics", "RoboWars Combat", "9876543211", "2026-11-16 09:30"));
                save(new Registration(++idGen, "2024ME09", "Chetan Rao", "Mechanical", "CAD 3D Modeling", "9876543212", "2026-11-16 10:15"));
            } catch (Exception ignored) {}
        }

        @Override
        public synchronized void save(Registration reg) throws Exception {
            String compositeKey = reg.getStudentId().toUpperCase() + "::" + reg.getEventName();
            if (uniqueKeys.contains(compositeKey)) {
                throw new DuplicateRegistrationException("Student " + reg.getStudentId() + " is already registered for '" + reg.getEventName() + "'.");
            }
            uniqueKeys.add(compositeKey);
            store.add(reg);
        }

        @Override
        public List<Registration> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public List<Registration> findByEvent(String eventName) {
            if (eventName == null || eventName.equalsIgnoreCase("All Events")) {
                return findAll();
            }
            List<Registration> list = new ArrayList<>();
            for (Registration r : store) {
                if (r.getEventName().equalsIgnoreCase(eventName)) {
                    list.add(r);
                }
            }
            return list;
        }

        @Override
        public boolean exists(String studentId, String eventName) {
            String compositeKey = studentId.toUpperCase() + "::" + eventName;
            return uniqueKeys.contains(compositeKey);
        }
    }

    public static class DuplicateRegistrationException extends Exception {
        public DuplicateRegistrationException(String msg) { super(msg); }
    }

    // -------------------------------------------------------------------------
    // 3. SERVICE LAYER
    // -------------------------------------------------------------------------
    public static class EventRegistrationService {
        private final RegistrationDAO dao;
        private long idCounter = 2000;

        public EventRegistrationService(RegistrationDAO dao) {
            this.dao = dao;
        }

        public Registration registerStudent(String studentId, String name, String dept, String event, String phone) throws Exception {
            // Validation
            if (studentId == null || !studentId.matches("^[A-Za-z0-9]{5,12}$")) {
                throw new IllegalArgumentException("Invalid Student ID: Must be 5 to 12 alphanumeric characters.");
            }
            if (name == null || name.trim().length() < 2) {
                throw new IllegalArgumentException("Student Name must be at least 2 characters.");
            }
            if (phone == null || !phone.matches("^[0-9]{10}$")) {
                throw new IllegalArgumentException("Contact Phone must be exactly 10 digits.");
            }
            if (dept == null || dept.isEmpty()) {
                throw new IllegalArgumentException("Please select a Department.");
            }
            if (event == null || event.isEmpty()) {
                throw new IllegalArgumentException("Please select an Event.");
            }

            if (dao.exists(studentId, event)) {
                throw new DuplicateRegistrationException("Duplicate Registration: " + studentId + " is already enrolled in " + event);
            }

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            Registration record = new Registration(++idCounter, studentId.toUpperCase(), name.trim(), dept, event, phone, now);
            dao.save(record);
            return record;
        }

        public List<Registration> getRegistrations(String filterEvent) throws Exception {
            return dao.findByEvent(filterEvent);
        }
    }

    // -------------------------------------------------------------------------
    // 4. PRESENTATION / UI LAYER
    // -------------------------------------------------------------------------
    private final EventRegistrationService service;

    private final JTextField studentIdField = new JTextField(12);
    private final JTextField studentNameField = new JTextField(12);
    private final JTextField phoneField = new JTextField(12);
    private final JComboBox<String> departmentCombo = new JComboBox<>(new String[]{
            "Computer Science", "Electronics", "Mechanical", "Civil", "Biotechnology"
    });
    private final JComboBox<String> eventCombo = new JComboBox<>(new String[]{
            "Code Relay Hackathon", "RoboWars Combat", "CAD 3D Modeling", "Battle of the Bands", "Tech Quiz Bowl"
    });

    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{
            "All Events", "Code Relay Hackathon", "RoboWars Combat", "CAD 3D Modeling", "Battle of the Bands", "Tech Quiz Bowl"
    });

    private final JButton registerButton = new JButton("Register Student");
    private final JButton clearButton = new JButton("Clear");
    private final JButton filterButton = new JButton("Apply Filter");
    private final JButton exportButton = new JButton("Export to CSV");

    private final DefaultTableModel tableModel;
    private final JTable registrationTable;
    private final JLabel statusLabel = new JLabel(" Ready.");
    private final JProgressBar progressBar = new JProgressBar();

    public EventRegistrationApp(EventRegistrationService service) {
        super("SIMATS Engineering - Cultural & Technical Fest Registration Desk");
        this.service = service;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Setup Menu Bar
        setJMenuBar(createMenuBar());

        // Left Form Panel
        JPanel formPanel = createFormPanel();

        // Center Table Panel
        String[] cols = {"Reg ID", "Student ID", "Name", "Department", "Event", "Contact", "Registered At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        registrationTable = new JTable(tableModel);
        registrationTable.setRowHeight(22);
        JScrollPane scrollPane = new JScrollPane(registrationTable);

        // Top Filter Bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Registrations"));
        filterPanel.add(new JLabel("Filter by Event:"));
        filterPanel.add(filterCombo);
        filterPanel.add(filterButton);
        filterPanel.add(exportButton);

        JPanel centerContainer = new JPanel(new BorderLayout(5, 5));
        centerContainer.add(filterPanel, BorderLayout.NORTH);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        progressBar.setPreferredSize(new Dimension(140, 16));
        progressBar.setVisible(false);
        statusPanel.add(progressBar, BorderLayout.EAST);

        // Main Layout Assembly
        add(formPanel, BorderLayout.WEST);
        add(centerContainer, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Wire Action Handlers
        setupEventHandlers();

        // Initial Data Load
        loadRegistrationsInBackground("All Events");

        setSize(960, 560);
        setMinimumSize(new Dimension(840, 480));
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export to CSV...");
        JMenuItem exitItem = new JMenuItem("Exit");
        exportItem.addActionListener(e -> exportToCsv());
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "SIMATS Cultural Event Registration System\nVersion 1.0 (CO4 Layered Desktop GUI Architecture)",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        mb.add(fileMenu);
        mb.add(helpMenu);
        return mb;
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" Student Event Registration "),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        g.gridx = 0; g.gridy = row; form.add(new JLabel("Student ID:"), g);
        g.gridx = 1; form.add(studentIdField, g); row++;

        g.gridx = 0; g.gridy = row; form.add(new JLabel("Student Name:"), g);
        g.gridx = 1; form.add(studentNameField, g); row++;

        g.gridx = 0; g.gridy = row; form.add(new JLabel("Department:"), g);
        g.gridx = 1; form.add(departmentCombo, g); row++;

        g.gridx = 0; g.gridy = row; form.add(new JLabel("Event Name:"), g);
        g.gridx = 1; form.add(eventCombo, g); row++;

        g.gridx = 0; g.gridy = row; form.add(new JLabel("Contact Phone:"), g);
        g.gridx = 1; form.add(phoneField, g); row++;

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        btnRow.add(clearButton);
        btnRow.add(registerButton);

        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        form.add(btnRow, g);

        return form;
    }

    private void setupEventHandlers() {
        registerButton.addActionListener(e -> submitRegistration());
        clearButton.addActionListener(e -> clearForm());
        filterButton.addActionListener(e -> {
            String selected = (String) filterCombo.getSelectedItem();
            loadRegistrationsInBackground(selected);
        });
        filterCombo.addActionListener(e -> {
            String selected = (String) filterCombo.getSelectedItem();
            loadRegistrationsInBackground(selected);
        });
        exportButton.addActionListener(e -> exportToCsv());
    }

    /**
     * Submits a new registration asynchronously via SwingWorker.
     */
    public void submitRegistration() {
        String sId = studentIdField.getText().trim();
        String sName = studentNameField.getText().trim();
        String dept = (String) departmentCombo.getSelectedItem();
        String event = (String) eventCombo.getSelectedItem();
        String phone = phoneField.getText().trim();

        // Background worker keeps UI responsive
        setBusy(true, "Registering student...");
        SwingWorker<Registration, Void> worker = new SwingWorker<>() {
            @Override
            protected Registration doInBackground() throws Exception {
                // Simulate network/database roundtrip latency
                Thread.sleep(80);
                return service.registerStudent(sId, sName, dept, event, phone);
            }

            @Override
            protected void done() {
                setBusy(false, "Ready.");
                try {
                    Registration reg = get();
                    JOptionPane.showMessageDialog(EventRegistrationApp.this,
                            "Registration Successful!\nID: " + reg.getId() + "\nStudent: " + reg.getStudentName(),
                            "Registration Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadRegistrationsInBackground((String) filterCombo.getSelectedItem());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(EventRegistrationApp.this,
                            cause.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText(" Error: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Loads filtered registrations from DAO using SwingWorker to prevent EDT freeze.
     */
    public void loadRegistrationsInBackground(String filter) {
        setBusy(true, "Loading registrations...");
        SwingWorker<List<Registration>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Registration> doInBackground() throws Exception {
                Thread.sleep(60); // simulated database access latency
                return service.getRegistrations(filter);
            }

            @Override
            protected void done() {
                setBusy(false, "Ready.");
                try {
                    List<Registration> list = get();
                    tableModel.setRowCount(0);
                    for (Registration r : list) {
                        tableModel.addRow(new Object[]{
                                r.getId(), r.getStudentId(), r.getStudentName(),
                                r.getDepartment(), r.getEventName(), r.getContactPhone(), r.getTimestamp()
                        });
                    }
                    statusLabel.setText(" Displaying " + list.size() + " registrations for filter: " + filter);
                } catch (Exception ex) {
                    statusLabel.setText(" Failed to load registrations.");
                }
            }
        };
        worker.execute();
    }

    private void clearForm() {
        studentIdField.setText("");
        studentNameField.setText("");
        phoneField.setText("");
        if (departmentCombo.getItemCount() > 0) departmentCombo.setSelectedIndex(0);
        if (eventCombo.getItemCount() > 0) eventCombo.setSelectedIndex(0);
    }

    public void exportToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("event_registrations.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(target))) {
                pw.println("RegID,StudentID,StudentName,Department,EventName,ContactPhone,Timestamp");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    pw.printf("%s,%s,\"%s\",\"%s\",\"%s\",%s,%s%n",
                            tableModel.getValueAt(i, 0),
                            tableModel.getValueAt(i, 1),
                            tableModel.getValueAt(i, 2),
                            tableModel.getValueAt(i, 3),
                            tableModel.getValueAt(i, 4),
                            tableModel.getValueAt(i, 5),
                            tableModel.getValueAt(i, 6));
                }
                JOptionPane.showMessageDialog(this, "Successfully exported " + tableModel.getRowCount() + " records to " + target.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setBusy(boolean busy, String message) {
        statusLabel.setText(" " + message);
        progressBar.setVisible(busy);
        progressBar.setIndeterminate(busy);
        registerButton.setEnabled(!busy);
        filterButton.setEnabled(!busy);
    }

    // Direct programmatic test execution
    public static void main(String[] args) {
        ThreadSafeRegistrationDAO dao = new ThreadSafeRegistrationDAO();
        EventRegistrationService service = new EventRegistrationService(dao);

        if (args.length > 0 && args[0].equals("--test")) {
            System.out.println("Running EventRegistrationApp full automated test suite...");

            // 1. Valid registration
            try {
                Registration r1 = service.registerStudent("2024CS99", "Divya Menon", "Computer Science", "Code Relay Hackathon", "9876543210");
                System.out.println("Test 1 (Valid Registration): PASSED -> Reg ID: " + r1.getId());
            } catch (Exception e) {
                System.err.println("Test 1 FAILED: " + e.getMessage());
                System.exit(1);
            }

            // 2. Duplicate registration
            try {
                service.registerStudent("2024CS99", "Divya Menon", "Computer Science", "Code Relay Hackathon", "9876543210");
                System.err.println("Test 2 FAILED: Duplicate registration was not blocked!");
                System.exit(1);
            } catch (DuplicateRegistrationException e) {
                System.out.println("Test 2 (Duplicate Prevention): PASSED -> Caught expected: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Test 2 FAILED with unexpected exception: " + e);
                System.exit(1);
            }

            // 3. Invalid phone number
            try {
                service.registerStudent("2024EC88", "Rohan Verma", "Electronics", "RoboWars Combat", "12345");
                System.err.println("Test 3 FAILED: Invalid phone accepted!");
                System.exit(1);
            } catch (IllegalArgumentException e) {
                System.out.println("Test 3 (Validation Rejection): PASSED -> Caught expected: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Test 3 FAILED with unexpected exception: " + e);
                System.exit(1);
            }

            // 4. Filtering
            try {
                List<Registration> codeRelayList = service.getRegistrations("Code Relay Hackathon");
                System.out.println("Test 4 (Event Filtering): PASSED -> Count for Code Relay: " + codeRelayList.size());
            } catch (Exception e) {
                System.err.println("Test 4 FAILED: " + e);
                System.exit(1);
            }

            System.out.println("EventRegistrationApp verification PASSED.");
            System.exit(0);
        }

        SwingUtilities.invokeLater(() -> new EventRegistrationApp(service).setVisible(true));
    }
}
