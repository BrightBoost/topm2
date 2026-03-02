package nl.topicus.jdbc.part2queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaSetup {
    public static void createStudentsTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS students (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100),
                    email VARCHAR(100),
                    age INT
                )
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'students' created (or already exists).");
        }
    }
}
