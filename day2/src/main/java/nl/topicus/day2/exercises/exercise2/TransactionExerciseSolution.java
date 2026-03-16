package nl.topicus.day2.exercises.exercise2;

import java.sql.*;

/**
 * Exercise 2 Solution: Transactions in JDBC
 *
 * Part 1: Happy path — course has 1 spot, enrollment + update both succeed, commit.
 * Part 2: Sad path — course is full, UPDATE fails, rollback undoes everything.
 * Part 3: Run both scenarios and compare results side by side.
 * Part 4 (Bonus): Savepoint — enroll in two courses, partial rollback.
 */
public class TransactionExerciseSolution {

    private static final String URL = "jdbc:h2:mem:exercise2;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static void main(String[] args) throws SQLException {
        System.out.println("=== Exercise 2: Transactions in JDBC ===\n");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Part 1: Happy path
            System.out.println("========================================");
            System.out.println("  Part 1: Happy Path — Commit");
            System.out.println("========================================\n");
            setupTables(conn, 1); // 1 available spot
            happyPath(conn);
            printDatabaseState(conn, "After Happy Path");

            // Part 2: Sad path
            System.out.println("\n========================================");
            System.out.println("  Part 2: Sad Path — Rollback");
            System.out.println("========================================\n");
            setupTables(conn, 0); // 0 available spots (full)
            sadPath(conn);
            printDatabaseState(conn, "After Sad Path");

            // Part 3: Comparison
            System.out.println("\n========================================");
            System.out.println("  Part 3: Comparison");
            System.out.println("========================================\n");
            System.out.println("Happy path: 1 enrollment, course has 0 spots — CONSISTENT ✓");
            System.out.println("Sad path:   0 enrollments, course has 0 spots — CONSISTENT ✓");
            System.out.println("Without transaction (exercise 1): 1 enrollment, 0 spots — INCONSISTENT ✗");
            System.out.println("The transaction prevents ghost registrations.");

            // Part 4 (Bonus): Savepoint
            System.out.println("\n========================================");
            System.out.println("  Part 4 (Bonus): Savepoint");
            System.out.println("========================================\n");
            savepointDemo(conn);
        }
    }

    // ======================== Part 1: Happy Path ========================

    private static void happyPath(Connection conn) throws SQLException {
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
            System.out.println("\n✓ Transaction COMMITTED.");

        } catch (SQLException e) {
            conn.rollback();
            System.out.println("\n✗ Transaction ROLLED BACK: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ======================== Part 2: Sad Path ========================

    private static void sadPath(Connection conn) throws SQLException {
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

            // UPDATE available spots — FAILS (CHECK constraint: 0 - 1 = -1)
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
    }

    // ======================== Part 4: Savepoint ========================

    private static void savepointDemo(Connection conn) throws SQLException {
        // Setup: two courses — one with spots, one full
        setupTables(conn, 0);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE courses SET available_spots = 5, name = 'Intro to SQL' WHERE id = 1");
            stmt.execute("INSERT INTO courses (name, available_spots) VALUES ('Advanced JDBC', 0)");
        }

        System.out.println("Course 1: 'Intro to SQL' — 5 spots");
        System.out.println("Course 2: 'Advanced JDBC' — 0 spots (full)");
        System.out.println("Enrolling Jan in both courses...\n");

        conn.setAutoCommit(false);
        try {
            // Enroll in course 1 (succeeds)
            enrollStudent(conn, "Jan", "jan@university.nl", 1);
            updateSpots(conn, 1);
            System.out.println("Enrolled in 'Intro to SQL' ✓");

            // Set savepoint
            Savepoint sp = conn.setSavepoint("afterFirstEnrollment");
            System.out.println("Savepoint set.");

            // Enroll in course 2 (fails — 0 spots)
            try {
                enrollStudent(conn, "Jan", "jan@university.nl", 2);
                updateSpots(conn, 2);
                System.out.println("Enrolled in 'Advanced JDBC' ✓");
            } catch (SQLException e) {
                System.out.println("Enrollment in 'Advanced JDBC' FAILED: " + e.getMessage());
                conn.rollback(sp);
                System.out.println("Rolled back to savepoint — first enrollment preserved.");
            }

            conn.commit();
            System.out.println("\n✓ Transaction COMMITTED (only first enrollment).");

        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Full rollback: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true);
        }

        printDatabaseState(conn, "After Savepoint Demo");
    }

    // ======================== Helpers ========================

    private static void setupTables(Connection conn, int availableSpots) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS enrollments");
            stmt.execute("DROP TABLE IF EXISTS courses");

            stmt.execute("""
                CREATE TABLE courses (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255),
                    available_spots INT CHECK (available_spots >= 0)
                )
            """);

            stmt.execute("""
                CREATE TABLE enrollments (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    student_name VARCHAR(255),
                    email VARCHAR(255),
                    course_id BIGINT,
                    FOREIGN KEY (course_id) REFERENCES courses(id)
                )
            """);

            stmt.execute("INSERT INTO courses (name, available_spots) VALUES ('Advanced JDBC', "
                    + availableSpots + ")");
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

    private static void printDatabaseState(Connection conn, String label) throws SQLException {
        System.out.println("\n--- " + label + " ---");
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM enrollments");
            boolean hasEnrollments = false;
            while (rs.next()) {
                hasEnrollments = true;
                System.out.println("Enrollment: " + rs.getString("student_name")
                        + " | " + rs.getString("email")
                        + " | course_id=" + rs.getLong("course_id"));
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
