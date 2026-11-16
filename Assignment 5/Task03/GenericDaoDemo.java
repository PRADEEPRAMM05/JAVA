package task03;

import java.sql.SQLException;
import java.util.*;

/**
 * GenericDaoDemo
 * Demonstrates the Data Access Object (DAO) pattern to eliminate boilerplate SQL
 * across student, course, and marks modules.
 */
public class GenericDaoDemo {

    // =========================================================================
    // 1. GENERIC DAO INTERFACE
    // =========================================================================
    public interface Dao<T, K> {
        void insert(T entity) throws SQLException;
        Optional<T> findById(K id) throws SQLException;
        List<T> findAll() throws SQLException;
        void update(T entity) throws SQLException;
        boolean delete(K id) throws SQLException;
    }

    // =========================================================================
    // 2. DOMAIN MODEL (Object-Relational Mapping target)
    // =========================================================================
    public static class Student {
        private final String regNo;        // Maps to column: reg_no (VARCHAR PRIMARY KEY)
        private String name;               // Maps to column: full_name (VARCHAR)
        private String email;              // Maps to column: email_id (VARCHAR UNIQUE)
        private String department;         // Maps to column: department (VARCHAR)
        private double cgpa;               // Maps to column: cgpa (DECIMAL(3,2))

        public Student(String regNo, String name, String email, String department, double cgpa) {
            this.regNo = regNo;
            this.name = name;
            this.email = email;
            this.department = department;
            this.cgpa = cgpa;
        }

        public String getRegNo() { return regNo; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDepartment() { return department; }
        public double getCgpa() { return cgpa; }
        public void setCgpa(double cgpa) { this.cgpa = cgpa; }

        @Override
        public String toString() {
            return String.format("Student[RegNo=%s, Name=%s, Dept=%s, CGPA=%.2f]", regNo, name, department, cgpa);
        }
    }

    // =========================================================================
    // 3. STUDENT DAO IMPLEMENTATION
    // =========================================================================
    public static class StudentDao implements Dao<Student, String> {
        // In-Memory simulated table store backing JDBC contract
        private final Map<String, Student> table = new LinkedHashMap<>();

        @Override
        public synchronized void insert(Student s) throws SQLException {
            if (table.containsKey(s.getRegNo())) {
                throw new SQLException("Primary key violation: Duplicate student regNo=" + s.getRegNo());
            }
            // Simulates: INSERT INTO students(reg_no, full_name, email_id, department, cgpa) VALUES (?, ?, ?, ?, ?)
            table.put(s.getRegNo(), new Student(s.getRegNo(), s.getName(), s.getEmail(), s.getDepartment(), s.getCgpa()));
        }

        @Override
        public synchronized Optional<Student> findById(String id) {
            // Simulates: SELECT * FROM students WHERE reg_no = ?
            Student s = table.get(id);
            return s != null ? Optional.of(new Student(s.getRegNo(), s.getName(), s.getEmail(), s.getDepartment(), s.getCgpa())) : Optional.empty();
        }

        @Override
        public synchronized List<Student> findAll() {
            // Simulates: SELECT * FROM students ORDER BY reg_no
            List<Student> list = new ArrayList<>();
            for (Student s : table.values()) {
                list.add(new Student(s.getRegNo(), s.getName(), s.getEmail(), s.getDepartment(), s.getCgpa()));
            }
            return list;
        }

        @Override
        public synchronized void update(Student s) throws SQLException {
            // Simulates: UPDATE students SET full_name=?, email_id=?, department=?, cgpa=? WHERE reg_no=?
            if (!table.containsKey(s.getRegNo())) {
                throw new SQLException("Student with regNo=" + s.getRegNo() + " does not exist.");
            }
            table.put(s.getRegNo(), new Student(s.getRegNo(), s.getName(), s.getEmail(), s.getDepartment(), s.getCgpa()));
        }

        @Override
        public synchronized boolean delete(String id) {
            // Simulates: DELETE FROM students WHERE reg_no = ?
            return table.remove(id) != null;
        }
    }

    public static void main(String[] args) throws SQLException {
        System.out.println("Running GenericDaoDemo verification...");
        Dao<Student, String> dao = new StudentDao();

        // 1. Insert
        dao.insert(new Student("192311001", "Aarav Patel", "aarav@simats.edu", "Computer Science", 9.15));
        dao.insert(new Student("192311002", "Bhavna Sharma", "bhavna@simats.edu", "Electronics", 8.85));

        // 2. Find By ID
        Optional<Student> student = dao.findById("192311001");
        assert student.isPresent() : "Student should exist";
        System.out.println("Found: " + student.get());

        // 3. Update
        Student s = student.get();
        s.setCgpa(9.40);
        dao.update(s);
        System.out.println("Updated CGPA: " + dao.findById("192311001").get().getCgpa());
        assert dao.findById("192311001").get().getCgpa() == 9.40;

        // 4. Find All
        List<Student> all = dao.findAll();
        System.out.println("Total Students: " + all.size());
        assert all.size() == 2;

        // 5. Delete
        boolean deleted = dao.delete("192311002");
        assert deleted && dao.findAll().size() == 1;
        System.out.println("Deleted 192311002. Remaining count: " + dao.findAll().size());

        System.out.println("GenericDaoDemo verification PASSED.");
    }
}
