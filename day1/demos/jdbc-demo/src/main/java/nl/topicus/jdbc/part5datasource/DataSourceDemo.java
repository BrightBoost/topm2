package nl.topicus.jdbc.part5datasource;

import nl.topicus.jdbc.part2queries.SchemaSetup;
import nl.topicus.jdbc.part4mapping.Student;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataSourceDemo {

    // Minimal DAO showing DataSource pattern
    static class StudentDaoWithDataSource {
        private final DataSource dataSource;

        StudentDaoWithDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public void save(Student student) {
            String sql = "INSERT INTO students(name, email, age) VALUES (?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, student.name());
                ps.setString(2, student.email());
                ps.setInt(3, student.age());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public List<Student> findAll() {
            String sql = "SELECT id, name, email, age FROM students ORDER BY id";
            List<Student> list = new ArrayList<>();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getInt("age")));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return list;
        }
    }

    public void runDemo() {
        DataSource ds = DatabaseConfig.createDataSource();

        // Use a direct connection only for one-off setup
        try (Connection conn = ds.getConnection()) {
            SchemaSetup.createStudentsTable(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        StudentDaoWithDataSource dao = new StudentDaoWithDataSource(ds);
        dao.save(new Student(0, "Eva", "eva@example.com", 27));
        dao.save(new Student(0, "Fred", "fred@example.com", 29));

        List<Student> all = dao.findAll();
        System.out.println("Students via DataSource DAO:");
        all.forEach(s -> System.out.println("  " + s));

        System.out.println("Each method gets and returns its own connection — this is the DataSource pattern.");
    }
}
