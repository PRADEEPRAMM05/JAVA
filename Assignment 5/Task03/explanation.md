# Task 03: Discover the Data-Access Design Pattern (Task A3)

## 1. Identification of the Common Pattern (Task A3.1)

In junior codebases featuring individual `StudentDB`, `CourseDB`, and `MarksDB` classes, over 80% of the codebase consists of redundant structural boilerplate:

| Repeated Boilerplate Steps | Variable Application Logic |
| :--- | :--- |
| 1. Obtaining a `Connection` from `DriverManager` or `DataSource` | Specific SQL statements (`INSERT`, `SELECT`, `UPDATE`, `DELETE`) |
| 2. Instantiating a `PreparedStatement` | Number and data types of query bind parameters (`ps.setString()`, etc.) |
| 3. Executing query (`executeQuery()` / `executeUpdate()`) | Specific domain table column names |
| 4. Iterating `ResultSet` while `rs.next()` is true | Instantiating specific domain objects (`new Student(...)` vs `new Course(...)`) |
| 5. Exception handling and translation of `SQLException` | Primary key identifier type (`String` regNo vs `int` courseId) |
| 6. Resource teardown via `close()` / try-with-resources | |

---

## 2. Generic Data-Access Interface Design (Task A3.2)

To eradicate this duplication, we introduce the **Generic DAO Pattern**:

```java
public interface Dao<T, K> {
    void insert(T entity) throws SQLException;
    Optional<T> findById(K id) throws SQLException;
    List<T> findAll() throws SQLException;
    void update(T entity) throws SQLException;
    boolean delete(K id) throws SQLException;
}
```

### Implementing `StudentDao`:
```java
public class StudentDao implements Dao<Student, String> {
    private final DataSource dataSource;

    public StudentDao(DataSource ds) { this.dataSource = ds; }

    @Override
    public Optional<Student> findById(String regNo) throws SQLException {
        String sql = "SELECT reg_no, full_name, email_id, department, cgpa FROM students WHERE reg_no = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Student(
                        rs.getString("reg_no"),
                        rs.getString("full_name"),
                        rs.getString("email_id"),
                        rs.getString("department"),
                        rs.getDouble("cgpa")
                    ));
                }
            }
        }
        return Optional.empty();
    }
    // insert, findAll, update, delete implemented symmetrically...
}
```

---

## 3. Object-to-Relational Mapping (Task A3.3)

Mapping Java `Student` domain fields to the SQL `students` table:

| Java Field (`Student` class) | SQL Column (`students` table) | SQL Data Type & Constraints | Mapping Role |
| :--- | :--- | :--- | :--- |
| `private final String regNo` | `reg_no` | `VARCHAR(32) PRIMARY KEY` | Immutable Entity Identifier |
| `private String name` | `full_name` | `VARCHAR(128) NOT NULL` | Student full legal name |
| `private String email` | `email_id` | `VARCHAR(128) NOT NULL UNIQUE` | Official communication email |
| `private String department` | `department` | `VARCHAR(64) NOT NULL` | Academic department code |
| `private double cgpa` | `cgpa` | `DECIMAL(3, 2) NOT NULL DEFAULT 0.00` | Cumulative Grade Point Average |

---

## 4. Alternative Concept and Complexity Analysis

### Comparison: Inline SQL in Service/GUI vs. Generic `Dao<T, K>`

```text
+------------------------------+------------------------------------+------------------------------------+
| Dimension                    | Inline SQL inside GUI / Services   | Generic DAO Architecture           |
+------------------------------+------------------------------------+------------------------------------+
| Ripple Effect of Column Rename| **High Blast Radius**: 25+ GUI and | **Zero Blast Radius**: Exactly 1   |
| (e.g. `full_name` -> `name`) | service files require edits        | file (`StudentDao`) requires edit  |
| Connection Leak Vulnerability| High (developers forget `close()`) | Low (encapsulated in DAO helper)   |
| Unit Testability             | Impossible without live database   | High (mock DAO via `Mockito`/stubs)|
| Runtime Execution Overhead   | O(1) direct query                  | O(1) method call delegation (<1 µs)|
| SQL Injection Surface Area   | High (scattered string concaten.)  | Zero (audited parameterized DAO)   |
+------------------------------+------------------------------------+------------------------------------+
```

**Conclusion:** The **Generic DAO pattern** introduces zero measurable runtime execution overhead while providing indispensable benefits: single-point schema refactoring, complete separation of UI from persistence, and testability.
