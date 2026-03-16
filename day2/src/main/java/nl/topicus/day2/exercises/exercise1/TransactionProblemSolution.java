package nl.topicus.day2.exercises.exercise1;

import nl.topicus.day2.demos.DatabaseUtil;

import java.sql.*;

/**
 * Exercise 1 Solution: Transaction Problem Without a Transaction
 *
 * Scenario: Enrolling a student in a course that is already full.
 * The INSERT into enrollments succeeds (auto-committed), but the UPDATE
 * on courses fails (CHECK constraint). Result: inconsistent data.
 *
 * Part 1: Set up courses + enrollments tables
 * Part 2: INSERT succeeds, UPDATE fails
 * Part 3: Inspect the inconsistent database state
 */
public class TransactionProblemSolution {

    private static final String URL = "jdbc:h2:mem:exercise1;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("=== Exercise 1: Transaction Problem Without a Transaction ===\n");

            // ===== Part 1: Database setup =====
            System.out.println("--- Part 1: Database Setup ---\n");
            setupDatabase(conn);

            // ===== Part 2: Simulate enrollment (INSERT succeeds, UPDATE fails) =====
            System.out.println("--- Part 2: Simulate Enrollment ---\n");
            simulateEnrollment(conn);

            // ===== Part 3: Inspect the damage =====
            System.out.println("--- Part 3: Inspect Database State ---\n");
            inspectDatabaseState(conn);
        }
    }

    /**
     * Part 1: Create tables with CHECK constraint and insert a full course.
     */
    private static void setupDatabase(Connection conn) throws SQLException {
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

            // Course with 0 spots — it's full!
            stmt.execute("INSERT INTO courses (name, available_spots) VALUES ('Advanced JDBC', 0)");
        }

        System.out.println("Tables created. Course 'Advanced JDBC' with 0 available spots.");
        System.out.println("Enrollments table is empty.\n");
    }

    /**
     * Part 2: Execute INSERT (succeeds) and UPDATE (fails).
     * Auto-commit is ON, so the INSERT is permanent before the UPDATE runs.
     */
    private static void simulateEnrollment(Connection conn) throws SQLException {
        // Step 1: INSERT into enrollments — auto-committed immediately
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO enrollments (student_name, email, course_id) VALUES (?, ?, ?)")) {
            ps.setString(1, "Jan");
            ps.setString(2, "jan@university.nl");
            ps.setLong(3, 1);
            int affected = ps.executeUpdate();
            System.out.println("INSERT enrollments: " + affected + " row(s) affected — committed immediately!");
        }

        // Step 2: UPDATE courses — fails because available_spots would become -1
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE courses SET available_spots = available_spots - 1 WHERE id = ?")) {
            ps.setLong(1, 1);
            int affected = ps.executeUpdate();
            System.out.println("UPDATE courses: " + affected + " row(s) affected");
        } catch (SQLException e) {
            System.out.println("UPDATE courses FAILED: " + e.getMessage());
            System.out.println("The CHECK constraint prevented available_spots from going below 0.\n");
        }
    }

    /**
     * Part 3: Query both tables to see the inconsistency.
     */
    private static void inspectDatabaseState(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Check enrollments
            ResultSet rs = stmt.executeQuery("SELECT * FROM enrollments");
            System.out.println("Enrollments:");
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println("  " + rs.getString("student_name")
                        + " | " + rs.getString("email")
                        + " | course_id=" + rs.getLong("course_id"));
            }
            if (!hasRows) {
                System.out.println("  (empty)");
            }

            // Check courses
            rs = stmt.executeQuery("SELECT * FROM courses");
            System.out.println("Courses:");
            while (rs.next()) {
                System.out.println("  " + rs.getString("name")
                        + " | available_spots=" + rs.getInt("available_spots"));
            }
        }

        System.out.println();
        System.out.println("PROBLEM: Jan is enrolled, but no spot was deducted!");
        System.out.println("The database is INCONSISTENT — a ghost registration.");
        System.out.println();
        // TODO: Jan staat ingeschreven maar er is geen plek afgetrokken.
        // Hoe zorgen we ervoor dat INSERT + UPDATE samen slagen of samen falen?
        // Antwoord: door een transactie te gebruiken (auto-commit uitzetten)
        System.out.println("Solution: use a transaction (setAutoCommit(false), commit/rollback).");
    }
}
