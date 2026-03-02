package nl.topicus.jdbc.part1intro;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDemo {

    public void runDemo() {
        // driver registration via ServiceLoader is automatic — no Class.forName() needed.
        String url = "jdbc:h2:file:./data/jdbcdemo";
        String user = "sa";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("Connected to: " + metaData.getDatabaseProductName() + " version " + metaData.getDatabaseProductVersion());
            System.out.println("Connection demo completed successfully.");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}
