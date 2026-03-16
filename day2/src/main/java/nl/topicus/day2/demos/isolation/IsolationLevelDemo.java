package nl.topicus.day2.demos.isolation;

import nl.topicus.day2.demos.DatabaseUtil;

import java.sql.*;

/**
 * Demo: Isolation Levels in JDBC.
 *
 * Shows how to set isolation levels and demonstrates the three
 * concurrency problems:
 * - Dirty Read
 * - Non-Repeatable Read
 * - Phantom Read
 *
 * Uses two connections from the same JVM to simulate concurrent transactions.
 */
public class IsolationLevelDemo {

    public static void main(String[] args) throws SQLException {
        System.out.println("========================================");
        System.out.println("  Isolation Levels Demo");
        System.out.println("========================================\n");

        showHowToSetIsolationLevel();
        demonstrateDirtyRead();
        demonstrateNonRepeatableRead();
        demonstratePhantomRead();
    }

    /**
     * Shows the syntax for setting isolation levels in JDBC.
     */
    private static void showHowToSetIsolationLevel() throws SQLException {
        System.out.println("--- Setting Isolation Levels ---\n");

        try (Connection conn = DatabaseUtil.getConnection()) {
            System.out.println("Default isolation level: " + isolationLevelName(conn.getTransactionIsolation()));

            conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            System.out.println("Set to: " + isolationLevelName(conn.getTransactionIsolation()));

            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            System.out.println("Set to: " + isolationLevelName(conn.getTransactionIsolation()));

            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            System.out.println("Set to: " + isolationLevelName(conn.getTransactionIsolation()));

            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            System.out.println("Set to: " + isolationLevelName(conn.getTransactionIsolation()));
        }
        System.out.println();
    }

    /**
     * Dirty Read: Transaction A reads uncommitted data from Transaction B.
     * With READ_UNCOMMITTED this is possible; with READ_COMMITTED it is prevented.
     */
    private static void demonstrateDirtyRead() throws SQLException {
        System.out.println("--- Dirty Read Demo ---\n");

        try (Connection connA = DatabaseUtil.getConnection();
             Connection connB = DatabaseUtil.getConnection()) {

            // Setup
            DatabaseUtil.setupStudentTable(connA);
            try (Statement stmt = connA.createStatement()) {
                stmt.execute("INSERT INTO students (name, email, age) VALUES ('Jan', 'jan@uni.nl', 20)");
            }

            // Transaction B: modify but don't commit
            connB.setAutoCommit(false);
            try (PreparedStatement ps = connB.prepareStatement(
                    "UPDATE students SET age = 25 WHERE name = 'Jan'")) {
                ps.executeUpdate();
                System.out.println("Transaction B: updated Jan's age to 25 (NOT committed)");
            }

            // Transaction A: try to read with READ_UNCOMMITTED
            connA.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            try (Statement stmt = connA.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT name, age FROM students WHERE name = 'Jan'");
                if (rs.next()) {
                    int age = rs.getInt("age");
                    System.out.println("Transaction A (READ_UNCOMMITTED): Jan's age = " + age);
                    if (age == 25) {
                        System.out.println("  → Dirty read! Read uncommitted value.");
                    } else {
                        System.out.println("  → H2 may prevent dirty reads even at READ_UNCOMMITTED (MVCC).");
                    }
                }
            }

            // Rollback B — the age 25 never really existed
            connB.rollback();
            connB.setAutoCommit(true);
            System.out.println("Transaction B: ROLLED BACK (age 25 never existed)\n");
        }
    }

    /**
     * Non-Repeatable Read: Same query returns different values within one transaction.
     */
    private static void demonstrateNonRepeatableRead() throws SQLException {
        System.out.println("--- Non-Repeatable Read Demo ---\n");

        try (Connection connA = DatabaseUtil.getConnection();
             Connection connB = DatabaseUtil.getConnection()) {

            // Setup
            DatabaseUtil.setupStudentTable(connA);
            try (Statement stmt = connA.createStatement()) {
                stmt.execute("INSERT INTO students (name, email, age) VALUES ('Jan', 'jan@uni.nl', 20)");
            }

            // Transaction A: first read
            connA.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connA.setAutoCommit(false);

            int firstRead;
            try (Statement stmt = connA.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT age FROM students WHERE name = 'Jan'");
                rs.next();
                firstRead = rs.getInt("age");
                System.out.println("Transaction A — first read: Jan's age = " + firstRead);
            }

            // Transaction B: update and commit
            try (PreparedStatement ps = connB.prepareStatement(
                    "UPDATE students SET age = 25 WHERE name = 'Jan'")) {
                ps.executeUpdate();
                System.out.println("Transaction B: updated Jan's age to 25 and auto-committed");
            }

            // Transaction A: second read (same query)
            try (Statement stmt = connA.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT age FROM students WHERE name = 'Jan'");
                rs.next();
                int secondRead = rs.getInt("age");
                System.out.println("Transaction A — second read: Jan's age = " + secondRead);

                if (firstRead != secondRead) {
                    System.out.println("  → Non-repeatable read! Same query, different result.");
                } else {
                    System.out.println("  → Read was repeatable (H2 MVCC snapshot behavior).");
                }
            }

            connA.commit();
            connA.setAutoCommit(true);
        }
        System.out.println();
    }

    /**
     * Phantom Read: New rows appear in a repeated query.
     */
    private static void demonstratePhantomRead() throws SQLException {
        System.out.println("--- Phantom Read Demo ---\n");

        try (Connection connA = DatabaseUtil.getConnection();
             Connection connB = DatabaseUtil.getConnection()) {

            // Setup
            DatabaseUtil.setupStudentTable(connA);
            try (Statement stmt = connA.createStatement()) {
                stmt.execute("INSERT INTO students (name, email, age) VALUES ('Jan', 'jan@uni.nl', 22)");
                stmt.execute("INSERT INTO students (name, email, age) VALUES ('Piet', 'piet@uni.nl', 25)");
            }

            // Transaction A: first count
            connA.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connA.setAutoCommit(false);

            int firstCount;
            try (Statement stmt = connA.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM students WHERE age > 20");
                rs.next();
                firstCount = rs.getInt("cnt");
                System.out.println("Transaction A — first query: " + firstCount + " students with age > 20");
            }

            // Transaction B: insert a new student and commit
            try (Statement stmt = connB.createStatement()) {
                stmt.execute("INSERT INTO students (name, email, age) VALUES ('Klaas', 'klaas@uni.nl', 23)");
                System.out.println("Transaction B: inserted Klaas (age 23) and auto-committed");
            }

            // Transaction A: repeat the same query
            try (Statement stmt = connA.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM students WHERE age > 20");
                rs.next();
                int secondCount = rs.getInt("cnt");
                System.out.println("Transaction A — second query: " + secondCount + " students with age > 20");

                if (secondCount > firstCount) {
                    System.out.println("  → Phantom read! A new row appeared.");
                } else {
                    System.out.println("  → No phantom read (H2 MVCC snapshot behavior).");
                }
            }

            connA.commit();
            connA.setAutoCommit(true);
        }
        System.out.println();
    }

    private static String isolationLevelName(int level) {
        return switch (level) {
            case Connection.TRANSACTION_NONE -> "NONE";
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ_UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "READ_COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE_READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "UNKNOWN (" + level + ")";
        };
    }
}
