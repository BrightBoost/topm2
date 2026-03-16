package nl.topicus.day2.demos.transactions;

import nl.topicus.day2.demos.DatabaseUtil;

import java.sql.*;

/**
 * Demo: Savepoints — partial rollback within a transaction.
 *
 * Scenario: A student enrolls in two courses at once. If enrollment in the
 * second course fails, we roll back only that part and keep the first enrollment.
 */
public class SavepointDemo {

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Setup: two courses — one with spots, one without
            DatabaseUtil.setupCourseTables(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO courses (name, available_spots) VALUES ('Intro to SQL', 5)");
                stmt.execute("INSERT INTO courses (name, available_spots) VALUES ('Advanced JDBC', 0)");
            }

            System.out.println("=== Savepoint Demo ===");
            System.out.println("Course 1: 'Intro to SQL' — 5 spots");
            System.out.println("Course 2: 'Advanced JDBC' — 0 spots (full)");
            System.out.println("Student enrolls in both. Second will fail.\n");

            conn.setAutoCommit(false);
            try {
                // Operation 1: Enroll in course 1 (succeeds)
                enrollStudent(conn, "Jan", "jan@university.nl", 1);
                updateSpots(conn, 1);
                System.out.println("Enrolled in 'Intro to SQL' ✓");

                // Set savepoint after successful first enrollment
                Savepoint sp = conn.setSavepoint("afterFirstEnrollment");
                System.out.println("Savepoint set.\n");

                // Operation 2: Attempt to enroll in course 2 (fails — 0 spots)
                try {
                    enrollStudent(conn, "Jan", "jan@university.nl", 2);
                    updateSpots(conn, 2);
                    System.out.println("Enrolled in 'Advanced JDBC' ✓");
                } catch (SQLException e) {
                    System.out.println("Enrollment in 'Advanced JDBC' FAILED: " + e.getMessage());
                    conn.rollback(sp);
                    System.out.println("Rolled back to savepoint — first enrollment preserved.\n");
                }

                // Commit: operation 1 is kept, operation 2 was rolled back
                conn.commit();
                System.out.println("Transaction COMMITTED.");

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Full rollback: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }

            // Verify
            System.out.println("\n--- Database State ---");
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM enrollments");
                while (rs.next()) {
                    System.out.println("Enrollment: " + rs.getString("student_name")
                            + " | course_id=" + rs.getLong("course_id"));
                }

                rs = stmt.executeQuery("SELECT * FROM courses");
                while (rs.next()) {
                    System.out.println("Course: " + rs.getString("name")
                            + " | available_spots=" + rs.getInt("available_spots"));
                }
            }

            System.out.println("\n✓ Jan is enrolled in 'Intro to SQL' (spots: 5→4)");
            System.out.println("✓ Jan is NOT enrolled in 'Advanced JDBC' (still full)");
        }
    }

    private static void enrollStudent(Connection conn, String name, String email, long courseId)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO enrollments (student_name, email, course_id) VALUES (?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setLong(3, courseId);
            ps.executeUpdate();
        }
    }

    private static void updateSpots(Connection conn, long courseId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE courses SET available_spots = available_spots - 1 WHERE id = ?")) {
            ps.setLong(1, courseId);
            ps.executeUpdate();
        }
    }
}
