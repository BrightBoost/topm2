package nl.topicus.studentdao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-implementatie van het StudentDao interface.
 *
 * In Mini Exercise 3 gebruikte deze class DriverManager met losse
 * url/user/password parameters. Jouw taak is om deze class te refactoren
 * zodat hij een DataSource gebruikt in plaats van DriverManager.
 */
public class JdbcStudentDao implements StudentDao {

    // TODO: Vervang deze drie velden door een enkel DataSource veld
    private final String url;
    private final String user;
    private final String password;

    // TODO: Pas de constructor aan zodat deze een DataSource ontvangt
    public JdbcStudentDao(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    // TODO: Pas deze methode aan om de connectie via de DataSource op te halen
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS students ("
            + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
            + "name VARCHAR(255), "
            + "email VARCHAR(255), "
            + "age INT)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Fout bij aanmaken tabel", e);
        }
    }

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id, name, email, age FROM students";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                students.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fout bij ophalen studenten", e);
        }
        return students;
    }

    @Override
    public Optional<Student> findById(long id) {
        String sql = "SELECT id, name, email, age FROM students WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fout bij zoeken student", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Student student) {
        String sql = "INSERT INTO students (name, email, age) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setInt(3, student.getAge());
            int rows = ps.executeUpdate();
            System.out.println("Opgeslagen: " + student.getName() + " (" + rows + " rij)");
        } catch (SQLException e) {
            throw new RuntimeException("Fout bij opslaan student", e);
        }
    }

    @Override
    public void update(Student student) {
        String sql = "UPDATE students SET name=?, email=?, age=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setInt(3, student.getAge());
            ps.setLong(4, student.getId());
            int rows = ps.executeUpdate();
            System.out.println("Updated rows: " + rows);
        } catch (SQLException e) {
            throw new RuntimeException("Fout bij updaten student", e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            System.out.println("Deleted rows: " + rows);
        } catch (SQLException e) {
            throw new RuntimeException("Fout bij verwijderen student", e);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getInt("age")
        );
    }
}
