package nl.topicus.day2.demos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Shared database utility for all demos and exercises.
 * Uses H2 in-memory database.
 */
public class DatabaseUtil {

    private static final String URL = "jdbc:h2:mem:day2demo;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void setupCourseTables(Connection conn) throws SQLException {
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
        }
    }

    public static void setupStudentTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS students");
            stmt.execute("""
                CREATE TABLE students (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255),
                    email VARCHAR(255) UNIQUE,
                    age INT,
                    version INT DEFAULT 1
                )
            """);
        }
    }
}
