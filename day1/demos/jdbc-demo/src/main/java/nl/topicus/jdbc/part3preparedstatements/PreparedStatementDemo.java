package nl.topicus.jdbc.part3preparedstatements;

import java.sql.*;

public class PreparedStatementDemo {

    public void runDemo() {
        String url = "jdbc:h2:file:./data/jdbcdemo";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            // Show safe lookups
            findByNameSafe(conn, "Alice");
            findByNameSafe(conn, "' OR '1'='1");
            System.out.println("✅ PreparedStatement prevented SQL injection.");

            // Also show safe INSERT with multiple setXxx()
            String insertSql = "INSERT INTO students(name, email, age) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, "Diana");
                ps.setString(2, "diana@example.com");
                ps.setInt(3, 28);
                int rows = ps.executeUpdate();
                System.out.println("Prepared INSERT affected rows: " + rows + "  → Diana");
            }
        } catch (SQLException e) {
            System.err.println("PreparedStatement demo failed: " + e.getMessage());
        }
    }

    private void findByNameSafe(Connection conn, String name) throws SQLException {
        String sql = "SELECT id, name, email, age FROM students WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name); // Parameters are bound — prevents SQL from being altered by input
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String nm = rs.getString("name");
                    String email = rs.getString("email");
                    int age = rs.getInt("age");
                    System.out.println("  [safe] [" + id + "] " + nm + " | " + email + " | age " + age);
                    count++;
                }
                if (name.equals("' OR '1'='1") && count == 0) {
                    System.out.println("Safe query for injection string: no rows found");
                } else {
                    System.out.println("Safe query for '" + name + "': found " + count + " row(s)");
                }
            }
        }
    }
}
