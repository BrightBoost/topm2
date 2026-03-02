package nl.topicus.jdbc.part3preparedstatements;

import nl.topicus.jdbc.part2queries.SchemaSetup;

import java.sql.*;

public class SqlInjectionDemo {

    public void runDemo() {
        String url = "jdbc:h2:file:./data/jdbcdemo";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            SchemaSetup.createStudentsTable(conn);

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM students"); // keep demo deterministic
                stmt.executeUpdate("INSERT INTO students(name, email, age) VALUES('Alice', 'alice@example.com', 22)");
                stmt.executeUpdate("INSERT INTO students(name, email, age) VALUES('Bob', 'bob@example.com', 25)");
            }

            findByNameUnsafe(conn, "Alice");
            findByNameUnsafe(conn, "' OR '1'='1");
            System.out.println("⚠️  SQL Injection succeeded! All rows returned.");
        } catch (SQLException e) {
            System.err.println("SQL Injection demo failed: " + e.getMessage());
        }
    }

    // Deliberately vulnerable: builds SQL via string concatenation
    private void findByNameUnsafe(Connection conn, String input) throws SQLException {
        String sql = "SELECT id, name, email, age FROM students WHERE name = '" + input + "'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                int age = rs.getInt("age");
                System.out.println("  [unsafe] [" + id + "] " + name + " | " + email + " | age " + age);
                count++;
            }
            System.out.println("Unsafe query for '" + input + "': found " + count + " row(s)");
        }
    }
}
