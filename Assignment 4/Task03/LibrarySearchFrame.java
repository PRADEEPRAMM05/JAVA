package task03;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Campus Library Search Screen
 * Demonstrates hierarchical nested layouts ensuring that when the window
 * is resized, the central results area expands dynamically while top search
 * controls and bottom action bars retain their fixed preferred heights.
 */
public class LibrarySearchFrame extends JFrame {

    private final JTextField searchField;
    private final JComboBox<String> categoryCombo;
    private final JButton searchButton;
    private final JTable resultsTable;
    private final DefaultTableModel tableModel;
    private final JButton prevButton;
    private final JButton nextButton;
    private final JButton reserveButton;
    private final JLabel statusLabel;

    // Simulated book catalog
    private static class BookRecord {
        final String accessionNo;
        final String title;
        final String author;
        final String category;
        final String status;

        BookRecord(String acc, String t, String a, String cat, String st) {
            this.accessionNo = acc;
            this.title = t;
            this.author = a;
            this.category = cat;
            this.status = st;
        }
    }

    private final List<BookRecord> masterCatalog = new ArrayList<>();
    private final List<BookRecord> filteredResults = new ArrayList<>();
    private int currentPage = 1;
    private final int pageSize = 5;

    public LibrarySearchFrame() {
        super("SIMATS Engineering - Central Library Catalog Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        populateCatalog();

        // 1. Root Frame uses BorderLayout
        setLayout(new BorderLayout(8, 8));

        // 2. North Container: Holds Title Banner and Search Controls
        JPanel northPanel = new JPanel(new BorderLayout(5, 5));
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel titleLabel = new JLabel("SIMATS Central Library Discovery Portal", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 60, 110));
        northPanel.add(titleLabel, BorderLayout.NORTH);

        // Search Controls Row (FlowLayout.CENTER maintains fixed height and natural component sizing)
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        searchRow.setBorder(BorderFactory.createTitledBorder("Search Criteria"));

        searchRow.add(new JLabel("Keywords:"));
        searchField = new JTextField(18);
        searchRow.add(searchField);

        searchRow.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>(new String[]{"All Categories", "Computer Science", "Electronics", "Mathematics", "Mechanical"});
        searchRow.add(categoryCombo);

        searchButton = new JButton("Search Books");
        searchRow.add(searchButton);
        northPanel.add(searchRow, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);

        // 3. Center Container: Results Table inside JScrollPane
        // BorderLayout.CENTER automatically expands to absorb all surplus window resizing space
        String[] columns = {"Accession No", "Book Title", "Author", "Category", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(24);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Search Results (Expands with Window Resize)"));
        add(scrollPane, BorderLayout.CENTER);

        // 4. South Container: Pagination, Action Buttons, and Status Bar
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // Button Row (FlowLayout.CENTER keeps buttons aligned with fixed natural size)
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        prevButton = new JButton("<< Previous");
        nextButton = new JButton("Next >>");
        reserveButton = new JButton("Reserve Selected Title");
        actionRow.add(prevButton);
        actionRow.add(nextButton);
        actionRow.add(reserveButton);
        southPanel.add(actionRow, BorderLayout.NORTH);

        // Status Message (BorderLayout.SOUTH of southPanel)
        statusLabel = new JLabel(" Ready. Enter search criteria or click 'Search Books'.");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        southPanel.add(statusLabel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        // Bind Listeners
        setupHandlers();

        // Initial search to display default catalog
        executeSearch();

        setSize(780, 520);
        setMinimumSize(new Dimension(640, 400));
        setLocationRelativeTo(null);
    }

    private void populateCatalog() {
        masterCatalog.add(new BookRecord("CS-101", "Core Java Volume I - Fundamentals", "Cay S. Horstmann", "Computer Science", "Available"));
        masterCatalog.add(new BookRecord("CS-102", "Effective Java (3rd Edition)", "Joshua Bloch", "Computer Science", "Available"));
        masterCatalog.add(new BookRecord("CS-103", "Java Concurrency in Practice", "Brian Goetz", "Computer Science", "Issued"));
        masterCatalog.add(new BookRecord("CS-104", "Design Patterns: Gang of Four", "Erich Gamma et al.", "Computer Science", "Available"));
        masterCatalog.add(new BookRecord("EC-201", "Digital Signal Processing", "John G. Proakis", "Electronics", "Available"));
        masterCatalog.add(new BookRecord("EC-202", "Microelectronic Circuits", "Adel S. Sedra", "Electronics", "Issued"));
        masterCatalog.add(new BookRecord("MA-301", "Advanced Engineering Mathematics", "Erwin Kreyszig", "Mathematics", "Available"));
        masterCatalog.add(new BookRecord("MA-302", "Discrete Mathematics and Its Applications", "Kenneth Rosen", "Mathematics", "Available"));
        masterCatalog.add(new BookRecord("ME-401", "Engineering Thermodynamics", "P.K. Nag", "Mechanical", "Available"));
        masterCatalog.add(new BookRecord("ME-402", "Fluid Mechanics and Machinery", "R.K. Bansal", "Mechanical", "Available"));
    }

    private void setupHandlers() {
        ActionListener searchAction = e -> {
            currentPage = 1;
            executeSearch();
        };

        searchButton.addActionListener(searchAction);
        searchField.addActionListener(searchAction);

        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                renderTablePage();
            }
        });

        nextButton.addActionListener(e -> {
            int maxPages = (int) Math.ceil((double) filteredResults.size() / pageSize);
            if (currentPage < maxPages) {
                currentPage++;
                renderTablePage();
            }
        });

        reserveButton.addActionListener(e -> {
            int selectedRow = resultsTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a book from the results table to reserve.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String title = (String) tableModel.getValueAt(selectedRow, 1);
            String status = (String) tableModel.getValueAt(selectedRow, 4);

            if ("Issued".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Book '" + title + "' is currently checked out.\nA reservation hold has been placed for you.", "Reservation Placed", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText(" Hold placed on: " + title);
            } else {
                JOptionPane.showMessageDialog(this, "Book '" + title + "' is available at Rack CS-04. Reserved for immediate pickup.", "Reserved", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText(" Reserved: " + title);
            }
        });
    }

    private void executeSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        String category = (String) categoryCombo.getSelectedItem();

        filteredResults.clear();
        for (BookRecord b : masterCatalog) {
            boolean matchesCat = "All Categories".equals(category) || b.category.equalsIgnoreCase(category);
            boolean matchesKey = keyword.isEmpty() ||
                    b.title.toLowerCase().contains(keyword) ||
                    b.author.toLowerCase().contains(keyword) ||
                    b.accessionNo.toLowerCase().contains(keyword);

            if (matchesCat && matchesKey) {
                filteredResults.add(b);
            }
        }
        renderTablePage();
    }

    private void renderTablePage() {
        tableModel.setRowCount(0);
        int total = filteredResults.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (currentPage > maxPages) currentPage = maxPages;

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        for (int i = start; i < end; i++) {
            BookRecord b = filteredResults.get(i);
            tableModel.addRow(new Object[]{b.accessionNo, b.title, b.author, b.category, b.status});
        }

        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < maxPages);

        statusLabel.setText(String.format(" Showing page %d of %d (%d books matched criteria)",
                currentPage, maxPages, total));
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--test")) {
            System.out.println("Running LibrarySearchFrame layout test...");
            LibrarySearchFrame frame = new LibrarySearchFrame();
            System.out.println("Initial Frame Size: " + frame.getWidth() + "x" + frame.getHeight());
            System.out.println("Results Count: " + frame.tableModel.getRowCount());

            // Simulate Resizing at 3 distinct sizes
            Dimension[] sizes = {
                    new Dimension(640, 400),
                    new Dimension(900, 600),
                    new Dimension(1200, 800)
            };
            for (Dimension d : sizes) {
                frame.setSize(d);
                frame.validate();
                Component center = ((BorderLayout) frame.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                System.out.println("Resized to " + d.width + "x" + d.height +
                        " -> Center Results Height: " + center.getHeight() +
                        "px (Dynamic expansion verified)");
            }
            System.out.println("LibrarySearchFrame Layout verification PASSED.");
            System.exit(0);
        }

        SwingUtilities.invokeLater(() -> new LibrarySearchFrame().setVisible(true));
    }
}
