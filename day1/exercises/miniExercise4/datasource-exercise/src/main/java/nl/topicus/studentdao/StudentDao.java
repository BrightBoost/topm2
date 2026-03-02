package nl.topicus.studentdao;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface voor Student entiteiten.
 *
 * Dit interface is hetzelfde als in Mini Exercise 3.
 */
public interface StudentDao {

    List<Student> findAll();

    Optional<Student> findById(long id);

    void save(Student student);

    void update(Student student);

    void delete(long id);
}
