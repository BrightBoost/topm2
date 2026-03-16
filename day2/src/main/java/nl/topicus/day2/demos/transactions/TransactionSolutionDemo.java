package nl.topicus.day2.demos.transactions;

import nl.topicus.day2.demos.DatabaseUtil;

import java.sql.*;

/**
 * Demo: Shows how transactions solve the inconsistency problem.
 * Demonstrates the try-catch-finally pattern with commit and rollback.
 *
 * Part 1: Happy path — course has spots, both operations succeed, commit.
 * Part 2: Sad path — course is full, UPDATE fails, rollback undoes everything.
 */
public class TransactionSolutionDemo {

    public static void main(String[] args) throws SQLException {
        System.out.println("========================================");
        System.out.println("  PART 1: Happy Path — Commit");
        System.out.println("========================================\n");
        happyPath();

        System.out.println("\n========================================");
        System.out.println("  PART 2: Sad Path — Rollback");
        System.out.println("========================================\n");
        sadPath();
    }

    private static void happyPath() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Setup: course with 1 available spot
            DatabaseUtil.setupCourseTables(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO courses (name, available_spots) VALUES ('Advanced JDBC', 1)");
            }

            System.out.println("Course 'Advanced JDBC' has 1 available spot.");
            System.out.println("Enrolling a student with transaction management...\n");

            // Transaction begins
            conn.setAutoCommit(false);
            try {
                // INSERT enrollment
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO enrollments (student_name, email, course_id) VALUES (?, ?, ?)")) {
                    ps.setString(1, "Jan");
                    ps.setString(2, "jan@university.nl");
                    ps.setLong(3, 1);
                    System.out.println("INSERT enrollments: " + ps.executeUpdate() + " row(s)");
                }

                // UPDATE available spots
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE courses SET available_spots = available_spots - 1 WHERE id = ?")) {
                    ps.setLong(1, 1);
                    System.out.println("UPDATE courses: " + ps.executeUpdate() + " row(s)");
                }

                conn.commit();
                System.out.println("\n✓ Transaction COMMITTED successfully.");

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("\n✗ Transaction ROLLED BACK: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }

            // Verify
            printDatabaseState(conn);
        }
    }

    private static void sadPath() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Setup: course with 0 available spots (full!)
            DatabaseUtil.setupCourseTables(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO courses (name, available_spots) VALUES ('Advanced JDBC', 0)");
            }

            System.out.println("Course 'Advanced JDBC' has 0 available spots (full).");
            System.out.println("Attempting enrollment with transaction management...\n");

            // Transaction begins
            conn.setAutoCommit(false);
            try {
                // INSERT enrollment — succeeds within the transaction (not yet committed)
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO enrollments (student_name, email, course_id) VALUES (?, ?, ?)")) {
                    ps.setString(1, "Jan");
                    ps.setString(2, "jan@university.nl");
                    ps.setLong(3, 1);
                    System.out.println("INSERT enrollments: " + ps.executeUpdate() + " row(s) (not yet committed)");
                }

                // UPDATE available spots — FAILS on CHECK constraint
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE courses SET available_spots = available_spots - 1 WHERE id = ?")) {
                    ps.setLong(1, 1);
                    ps.executeUpdate();
                }

                conn.commit();

            } catch (SQLException e) {
                System.out.println("UPDATE failed: " + e.getMessage());
                conn.rollback();
                System.out.println("\n✓ Transaction ROLLED BACK — no ghost registration!");
            } finally {
                conn.setAutoCommit(true);
            }

            // Verify: enrollments should be empty
            printDatabaseState(conn);
        }
    }

    private static void printDatabaseState(Connection conn) throws SQLException {
        System.out.println("\n--- Database State ---");
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM enrollments");
            boolean hasEnrollments = false;
            while (rs.next()) {
                hasEnrollments = true;
                System.out.println("Enrollment: " + rs.getString("student_name")
                        + " | " + rs.getString("email"));
            }
            if (!hasEnrollments) {
                System.out.println("Enrollments: (empty)");
            }

            rs = stmt.executeQuery("SELECT * FROM courses");
            while (rs.next()) {
                System.out.println("Course: " + rs.getString("name")
                        + " | available_spots=" + rs.getInt("available_spots"));
            }
        }
    }
}
