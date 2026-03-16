package nl.topicus.day2.demos.locking;

import nl.topicus.day2.demos.DatabaseUtil;

import java.sql.*;

/**
 * Demo: The Lost Update Problem and Pessimistic Locking with SELECT ... FOR UPDATE.
 *
 * Shows how two concurrent transactions can cause a lost update,
 * and how pessimistic locking prevents it.
 */
public class PessimisticLockingDemo {

    public static void main(String[] args) throws SQLException {
        System.out.println("========================================");
        System.out.println("  Pessimistic Locking Demo");
        System.out.println("========================================\n");

        demonstrateLostUpdate();
        demonstratePessimisticLocking();
    }

    /**
     * Lost Update: Two transactions read the same value, both update based on the
     * old value, and the first update is overwritten.
     */
    private static void demonstrateLostUpdate() throws SQLException {
        System.out.println("--- Lost Update Problem ---\n");

        try (Connection connA = DatabaseUtil.getConnection();
             Connection connB = DatabaseUtil.getConnection()) {

            DatabaseUtil.setupCourseTables(connA);
            try (Statement stmt = connA.createStatement()) {
                stmt.execute("INSERT INTO courses (name, available_spots) VALUES ('Popular Course', 10)");
            }
            System.out.println("Course 'Popular Course' starts with 10 spots.\n");

            connA.setAutoCommit(false);
            connB.setAutoCommit(false);

            // Both transactions read spots = 10
            int spotsA = readSpots(connA, 1);
            System.out.println("Transaction A reads: spots = " + spotsA);
            int spotsB = readSpots(connB, 1);
            System.out.println("Transaction B reads: spots = " + spotsB);

            // Both decrement by 1 and write — based on the OLD value
            updateSpots(connA, 1, spotsA - 1);
            connA.commit();
            System.out.println("Transaction A writes spots = " + (spotsA - 1) + " and commits");

            updateSpots(connB, 1, spotsB - 1);
            connB.commit();
            System.out.println("Transaction B writes spots = " + (spotsB - 1) + " and commits");

            connA.setAutoCommit(true);
            connB.setAutoCommit(true);

            // Check: should be 8, but is 9!
            int finalSpots = readSpots(connA, 1);
            System.out.println("\nFinal spots: " + finalSpots
                    + " (expected 8, but got 9 — one update was LOST!)\n");
        }
    }

    /**
     * Pessimistic Locking: SELECT ... FOR UPDATE locks the row so
     * the second transaction must wait.
     */
    private static void demonstratePessimisticLocking() throws SQLException {
        System.out.println("--- Pessimistic Locking: SELECT ... FOR UPDATE ---\n");

        try (Connection conn = DatabaseUtil.getConnection()) {
            DatabaseUtil.setupCourseTables(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO courses (name, available_spots) VALUES ('Popular Course', 10)");
            }

            System.out.println("Course 'Popular Course' starts with 10 spots.");
            System.out.println("Using SELECT ... FOR UPDATE to lock the row:\n");

            conn.setAutoCommit(false);
            try {
                // Lock the row with FOR UPDATE
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM courses WHERE id = ? FOR UPDATE")) {
                    ps.setLong(1, 1);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int spots = rs.getInt("available_spots");
                        System.out.println("Read (with lock): spots = " + spots);

                        // Safely update — no one else can touch this row
                        try (PreparedStatement update = conn.prepareStatement(
                                "UPDATE courses SET available_spots = ? WHERE id = ?")) {
                            update.setInt(1, spots - 1);
                            update.setLong(2, 1);
                            update.executeUpdate();
                            System.out.println("Updated spots to " + (spots - 1));
                        }
                    }
                }
                conn.commit();
                System.out.println("Committed. Row lock released.\n");
            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }

            int finalSpots = readSpots(conn, 1);
            System.out.println("Final spots: " + finalSpots + " ✓");
            System.out.println("\nNote: In a real concurrent scenario, Transaction B would WAIT");
            System.out.println("until Transaction A commits or rolls back before it can lock the row.");
        }
    }

    private static int readSpots(Connection conn, long courseId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT available_spots FROM courses WHERE id = ?")) {
            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("available_spots");
            }
        }
    }

    private static void updateSpots(Connection conn, long courseId, int newSpots) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE courses SET available_spots = ? WHERE id = ?")) {
            ps.setInt(1, newSpots);
            ps.setLong(2, courseId);
            ps.executeUpdate();
        }
    }
}
