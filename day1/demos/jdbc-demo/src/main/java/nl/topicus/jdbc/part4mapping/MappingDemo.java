package nl.topicus.jdbc.part4mapping;

import nl.topicus.jdbc.part2queries.SchemaSetup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MappingDemo {

    public void runDemo() {
        String url = "jdbc:h2:file:./data/jdbcdemo";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            SchemaSetup.createStudentsTable(conn);

            StudentDao dao = new StudentDaoImpl(conn);

            dao.save(new Student(0, "Anna", "anna@example.com", 21));
            dao.save(new Student(0, "Bert", "bert@example.com", 23));
            dao.save(new Student(0, "Carla", "carla@example.com", 24));

            System.out.println("All via DAO:");
            List<Student> all = dao.findAll();
            all.forEach(s -> System.out.println("  " + s));

            Optional<Student> maybeFirst = dao.findById(1);
            System.out.println("findById(1): " + maybeFirst.orElse(null));

            Optional<Student> maybeMissing = dao.findById(9999);
            System.out.println("findById(9999): " + (maybeMissing.isPresent() ? maybeMissing.get() : "<empty>"));

            if (!all.isEmpty()) {
                Student first = all.get(0);
                Student updated = new Student(first.id(), first.name() + " Updated", first.email(), first.age() + 1);
                dao.update(updated);
            }

            System.out.println("After update:");
            dao.findAll().forEach(s -> System.out.println("  " + s));

            if (dao.findAll().size() > 1) {
                long idToDelete = dao.findAll().get(1).id();
                dao.delete(idToDelete);
            }

            System.out.println("After delete:");
            dao.findAll().forEach(s -> System.out.println("  " + s));
        } catch (SQLException e) {
            System.err.println("Mapping demo failed: " + e.getMessage());
        }
    }
}
