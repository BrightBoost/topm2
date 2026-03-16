package nl.topicus.day2.demos.locking;

import nl.topicus.day2.demos.DatabaseUtil;

import java.sql.*;

/**
 * Demo: Optimistic Locking with a version column.
 *
 * Shows the pattern: UPDATE ... SET version = version + 1 WHERE id = ? AND version = ?
 * If executeUpdate() returns 0, a conflict is detected.
 */
public class OptimisticLockingDemo {

    public static void main(String[] args) throws SQLException {
        System.out.println("========================================");
        System.out.println("  Optimistic Locking Demo");
        System.out.println("========================================\n");

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Setup: students table with version column
            DatabaseUtil.setupStudentTable(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO students (name, email, age) VALUES ('Jan', 'jan@uni.nl', 20)");
            }

            // Read student
            long studentId;
            int version;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM students WHERE email = 'jan@uni.nl'");
                rs.next();
                studentId = rs.getLong("id");
                version = rs.getInt("version");
                System.out.println("Read student: Jan, age=20, version=" + version);
            }

            // Successful update (version matches)
            System.out.println("\n--- Update 1: Normal update ---");
            int rows = updateWithVersion(conn, studentId, "Jan de Vries", 21, version);
            System.out.println("Rows updated: " + rows + " ✓");

            // Show new state
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM students WHERE id = " + studentId);
                rs.next();
                System.out.println("After update: name=" + rs.getString("name")
                        + ", age=" + rs.getInt("age")
                        + ", version=" + rs.getInt("version"));
            }

            // Conflict! Try to update with the OLD version
            System.out.println("\n--- Update 2: Conflict (stale version) ---");
            System.out.println("Another user tries to update with version=" + version + " (stale!)");
            rows = updateWithVersion(conn, studentId, "Piet", 30, version);
            if (rows == 0) {
                System.out.println("Rows updated: 0 — CONFLICT DETECTED!");
                System.out.println("The data was modified by someone else.");
                System.out.println("In a real app: throw OptimisticLockException.");
            }

            // Database is unchanged
            System.out.println("\n--- Final state ---");
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM students WHERE id = " + studentId);
                rs.next();
                System.out.println("Student: name=" + rs.getString("name")
                        + ", age=" + rs.getInt("age")
                        + ", version=" + rs.getInt("version"));
                System.out.println("✓ The conflicting update was safely rejected.");
            }
        }
    }

    private static int updateWithVersion(Connection conn, long id, String name, int age, int expectedVersion)
            throws SQLException {
        String sql = "UPDATE students SET name = ?, age = ?, version = version + 1 "
                   + "WHERE id = ? AND version = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setLong(3, id);
            ps.setInt(4, expectedVersion);
            return ps.executeUpdate();
        }
    }
}
