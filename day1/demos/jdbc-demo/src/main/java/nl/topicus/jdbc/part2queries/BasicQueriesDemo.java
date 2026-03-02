package nl.topicus.jdbc.part2queries;

import java.sql.*;

public class BasicQueriesDemo {

    public void runDemo() {
        String url = "jdbc:h2:file:./data/jdbcdemo";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            SchemaSetup.createStudentsTable(conn);

            try (Statement stmt = conn.createStatement()) {
                int rows1 = stmt.executeUpdate("INSERT INTO students(name, email, age) VALUES('Alice', 'alice@example.com', 22)");
                System.out.println("INSERT affected rows: " + rows1 + "  → Alice");

                int rows2 = stmt.executeUpdate("INSERT INTO students(name, email, age) VALUES('Bob', 'bob@example.com', 25)");
                System.out.println("INSERT affected rows: " + rows2 + "  → Bob");

                int rows3 = stmt.executeUpdate("INSERT INTO students(name, email, age) VALUES('Charlie', 'charlie@example.com', 20)");
                System.out.println("INSERT affected rows: " + rows3 + "  → Charlie");
            }

            System.out.println("All students:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name, email, age FROM students ORDER BY id")) {
                // rs.next() moves the cursor forward — starts before the first row.
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    int age = rs.getInt("age");
                    System.out.println("  [" + id + "] " + name + " | " + email + " | age " + age);
                }
            }

            try (Statement stmt = conn.createStatement()) {
                int updated = stmt.executeUpdate("UPDATE students SET age = 26 WHERE name = 'Bob'");
                System.out.println("UPDATE affected rows: " + updated);

                int deleted = stmt.executeUpdate("DELETE FROM students WHERE name = 'Charlie'");
                System.out.println("DELETE affected rows: " + deleted);
            }

            System.out.println("Remaining students:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name, email, age FROM students ORDER BY id")) {
                int count = 0;
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    int age = rs.getInt("age");
                    System.out.println("  [" + id + "] " + name + " | " + email + " | age " + age);
                    count++;
                }
                System.out.println("Remaining students: " + count);
            }
        } catch (SQLException e) {
            System.err.println("Basic queries demo failed: " + e.getMessage());
        }
    }
}
