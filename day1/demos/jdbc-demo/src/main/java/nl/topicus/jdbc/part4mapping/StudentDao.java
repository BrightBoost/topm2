package nl.topicus.jdbc.part4mapping;

import java.util.List;
import java.util.Optional;

public interface StudentDao {
    List<Student> findAll();
    Optional<Student> findById(long id);
    void save(Student student);
    void update(Student student);
    void delete(long id);
}
