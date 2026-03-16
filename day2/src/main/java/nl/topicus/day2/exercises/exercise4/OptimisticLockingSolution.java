package nl.topicus.day2.exercises.exercise4;

import java.sql.*;

/**
 * Exercise 4 Solution: Optimistic Locking in Action
 *
 * Part 1: Create students table with version column
 * Part 2: Update with version check (happy path)
 * Part 3: Simulate conflict (stale version → 0 rows updated)
 * Part 4: Conflict handling with exception and re-read
 */
public class OptimisticLockingSolution {

    private static final String URL = "jdbc:h2:mem:exercise4;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static void main(String[] args) throws SQLException {
        System.out.println("=== Exercise 4: Optimistic Locking in Action ===\n");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Part 1: Setup
            System.out.println("========================================");
            System.out.println("  Part 1: Table Setup with Version Column");
            System.out.println("========================================\n");
            setupDatabase(conn);
            long studentId = verifySetup(conn);

            // Part 2: Update with version check
            System.out.println("========================================");
            System.out.println("  Part 2: Update with Version Check");
            System.out.println("========================================\n");
            int currentVersion = updateWithVersionCheck(conn, studentId);

            // Part 3: Simulate conflict
            System.out.println("========================================");
            System.out.println("  Part 3: Simulate Conflict");
            System.out.println("========================================\n");
            simulateConflict(conn, studentId, 1); // using stale version 1

            // Part 4: Conflict handling
            System.out.println("========================================");
            System.out.println("  Part 4: Conflict Handling");
            System.out.println("========================================\n");
            handleConflictGracefully(conn, studentId);
        }
    }

    // ======================== Part 1: Setup ========================

    private static void setupDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS students");
            stmt.execute("""
                CREATE TABLE students (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255),
                    email VARCHAR(255) UNIQUE,
                    age INT,
                    version INT DEFAULT 1
                )
            """);

            stmt.execute("INSERT INTO students (name, email, age) VALUES ('Jan', 'jan@university.nl', 20)");
        }
        System.out.println("Table 'students' created with version column (DEFAULT 1).");
        System.out.println("Inserted student: Jan, jan@university.nl, age 20.\n");
    }

    private static long verifySetup(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");
            rs.next();
            long id = rs.getLong("id");
            System.out.println("Verify: id=" + id
                    + ", name=" + rs.getString("name")
                    + ", email=" + rs.getString("email")
                    + ", age=" + rs.getInt("age")
                    + ", version=" + rs.getInt("version"));
            System.out.println("✓ version = 1 (default)\n");
            return id;
        }
    }

    // ======================== Part 2: Versioned Update ========================

    private static int updateWithVersionCheck(Connection conn, long studentId) throws SQLException {
        // Read current state
        int currentVersion;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, email, age, version FROM students WHERE id = ?")) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                currentVersion = rs.getInt("version");
                System.out.println("Read student: version=" + currentVersion);
            }
        }

        // Update with version check
        String updateSql = "UPDATE students SET name = ?, age = ?, version = version + 1 "
                         + "WHERE id = ? AND version = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, "Jan de Vries");
            ps.setInt(2, 23);
            ps.setLong(3, studentId);
            ps.setInt(4, currentVersion);
            int rowsUpdated = ps.executeUpdate();
            System.out.println("Rows updated: " + rowsUpdated + " ✓");
        }

        // Verify new state
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM students WHERE id = ?")) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int newVersion = rs.getInt("version");
                System.out.println("After update: name=" + rs.getString("name")
                        + ", age=" + rs.getInt("age")
                        + ", version=" + newVersion);
                System.out.println("✓ version incremented from " + currentVersion + " to " + newVersion + "\n");
                return newVersion;
            }
        }
    }

    // ======================== Part 3: Conflict Simulation ========================

    private static void simulateConflict(Connection conn, long studentId, int staleVersion)
            throws SQLException {
        System.out.println("Attempting update with STALE version=" + staleVersion
                + " (database has version=2)...\n");

        String updateSql = "UPDATE students SET name = ?, age = ?, version = version + 1 "
                         + "WHERE id = ? AND version = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, "Piet Jansen");
            ps.setInt(2, 30);
            ps.setLong(3, studentId);
            ps.setInt(4, staleVersion); // stale version!
            int rowsUpdated = ps.executeUpdate();

            System.out.println("Rows updated: " + rowsUpdated);
            if (rowsUpdated == 0) {
                System.out.println("CONFLICT DETECTED: data was modified by another user!");
                System.out.println("The stale update was safely rejected.");
            }
        }

        // Verify data is unchanged
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM students WHERE id = ?")) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("Database still has: name=" + rs.getString("name")
                        + ", age=" + rs.getInt("age")
                        + ", version=" + rs.getInt("version"));
                System.out.println("✓ No data was overwritten.\n");
            }
        }
    }

    // ======================== Part 4: Conflict Handling ========================

    private static void handleConflictGracefully(Connection conn, long studentId) throws SQLException {
        System.out.println("Attempting update with stale version to trigger proper exception handling...\n");

        try {
            updateStudentWithConflictCheck(conn, studentId, "Klaas", 25, 1);
        } catch (OptimisticLockException e) {
            System.out.println("Exception caught: " + e.getMessage());
            System.out.println("\nRe-reading current state from database...");

            // Re-read the student to get fresh data
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM students WHERE id = ?")) {
                ps.setLong(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    System.out.println("Current state: name=" + rs.getString("name")
                            + ", age=" + rs.getInt("age")
                            + ", version=" + rs.getInt("version"));
                    System.out.println("\n✓ Conflict handled gracefully. User can now retry with fresh data.");
                }
            }
        }
    }

    private static void updateStudentWithConflictCheck(Connection conn, long studentId,
            String name, int age, int expectedVersion) throws SQLException {
        String updateSql = "UPDATE students SET name = ?, age = ?, version = version + 1 "
                         + "WHERE id = ? AND version = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setLong(3, studentId);
            ps.setInt(4, expectedVersion);
            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated == 0) {
                throw new OptimisticLockException(
                        "Optimistic lock conflict: student (id=" + studentId
                                + ") was modified by another user. Expected version="
                                + expectedVersion);
            }
        }
    }
}
