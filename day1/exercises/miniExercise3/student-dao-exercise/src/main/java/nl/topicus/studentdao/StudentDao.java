package nl.topicus.studentdao;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface voor Student entiteiten.
 *
 * Dit interface definieert de standaard CRUD-operaties.
 * Jouw taak is om dit interface te implementeren met JDBC en PreparedStatements.
 */
public interface StudentDao {

    /**
     * Haal alle studenten op uit de database.
     * @return een lijst van alle studenten
     */
    List<Student> findAll();

    /**
     * Zoek een student op basis van ID.
     * @param id het ID van de student
     * @return een Optional met de student, of empty als die niet bestaat
     */
    Optional<Student> findById(long id);

    /**
     * Sla een nieuwe student op in de database.
     * @param student de student om op te slaan
     */
    void save(Student student);

    /**
     * Werk een bestaande student bij in de database.
     * @param student de student met bijgewerkte gegevens
     */
    void update(Student student);

    /**
     * Verwijder een student op basis van ID.
     * @param id het ID van de student om te verwijderen
     */
    void delete(long id);
}
