package nl.topicus.day2.demos.transactions;

import nl.topicus.day2.demos.DatabaseUtil;

import java.sql.*;

/**
 * Demo: Shows what happens when two related operations are executed
 * WITHOUT transaction management. The INSERT succeeds but the UPDATE
 * fails, leaving the database in an inconsistent state.
 *
 * This is the "problem" demo that motivates why we need transactions.
 */
public class TransactionProblemDemo {

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Setup: course with 0 available spots (full!)
            DatabaseUtil.setupCourseTables(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO courses (name, available_spots) VALUES ('Advanced JDBC', 0)");
            }

            System.out.println("=== Demo: Transaction Problem WITHOUT Transaction ===");
            System.out.println("Course 'Advanced JDBC' has 0 available spots (full).");
            System.out.println("Attempting to enroll a student...\n");

            // Auto-commit is ON by default — each statement commits immediately

            // Step 1: INSERT into enrollments — this succeeds and is committed immediately
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO enrollments (student_name, email, course_id) VALUES (?, ?, ?)")) {
                ps.setString(1, "Jan");
                ps.setString(2, "jan@university.nl");
                ps.setLong(3, 1);
                int rows = ps.executeUpdate();
                System.out.println("Step 1 - INSERT enrollments: " + rows + " row(s) affected ✓");
            }

            // Step 2: UPDATE courses — this FAILS because of CHECK constraint (0 - 1 = -1)
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE courses SET available_spots = available_spots - 1 WHERE id = ?")) {
                ps.setLong(1, 1);
                ps.executeUpdate();
                System.out.println("Step 2 - UPDATE courses: succeeded");
            } catch (SQLException e) {
                System.out.println("Step 2 - UPDATE courses: FAILED! " + e.getMessage());
            }

            // Inspect the damage
            System.out.println("\n--- Database State (inconsistent!) ---");
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM enrollments");
                while (rs.next()) {
                    System.out.println("Enrollment: " + rs.getString("student_name")
                            + " | " + rs.getString("email")
                            + " | course_id=" + rs.getLong("course_id"));
                }

                rs = stmt.executeQuery("SELECT * FROM courses");
                while (rs.next()) {
                    System.out.println("Course: " + rs.getString("name")
                            + " | available_spots=" + rs.getInt("available_spots"));
                }
            }

            System.out.println("\n⚠ PROBLEM: Jan is enrolled but no spot was deducted!");
            System.out.println("The database is now INCONSISTENT — a ghost registration.");
            System.out.println("Solution: use a transaction (see TransactionSolutionDemo).");
        }
    }
}
