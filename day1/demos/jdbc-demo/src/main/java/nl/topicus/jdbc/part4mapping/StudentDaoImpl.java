package nl.topicus.jdbc.part4mapping;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDaoImpl implements StudentDao {
    private final Connection conn;

    public StudentDaoImpl(Connection conn) {
        this.conn = conn;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        int age = rs.getInt("age");
        return new Student(id, name, email, age);
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT id, name, email, age FROM students ORDER BY id";
        List<Student> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Optional<Student> findById(long id) {
        String sql = "SELECT id, name, email, age FROM students WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Student student) {
        String sql = "INSERT INTO students(name, email, age) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.name());
            ps.setString(2, student.email());
            ps.setInt(3, student.age());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Student student) {
        String sql = "UPDATE students SET name = ?, email = ?, age = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.name());
            ps.setString(2, student.email());
            ps.setInt(3, student.age());
            ps.setLong(4, student.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
