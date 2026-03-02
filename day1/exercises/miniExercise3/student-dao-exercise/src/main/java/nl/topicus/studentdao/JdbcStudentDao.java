package nl.topicus.studentdao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-implementatie van het StudentDao interface.
 *
 * Alle methodes moeten PreparedStatements gebruiken.
 */
public class JdbcStudentDao implements StudentDao {

    private final String url;
    private final String user;
    private final String password;

    public JdbcStudentDao(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Maakt de students-tabel aan als deze nog niet bestaat.
     */
    public void createTable() {
        // TODO: Maak de tabel "students" aan met kolommen: id (auto-increment), name, email, age
        //       Gebruik CREATE TABLE IF NOT EXISTS
    }

    @Override
    public List<Student> findAll() {
        // TODO: Haal alle studenten op met een SELECT query
        //       Gebruik een PreparedStatement
        //       Map elke rij naar een Student object
        //       Return de lijst
        return new ArrayList<>();
    }

    @Override
    public Optional<Student> findById(long id) {
        // TODO: Zoek een student op ID met een PreparedStatement
        //       Return Optional.of(student) als gevonden, anders Optional.empty()
        return Optional.empty();
    }

    @Override
    public void save(Student student) {
        // TODO: Voeg een nieuwe student toe met een INSERT PreparedStatement
        //       Gebruik de velden name, email en age van de student
        //       Print een bevestiging naar de console
    }

    @Override
    public void update(Student student) {
        // TODO: Werk een bestaande student bij op basis van ID
        //       Update name, email en age
        //       Print het aantal affected rows
    }

    @Override
    public void delete(long id) {
        // TODO: Verwijder een student op basis van ID
        //       Print het aantal affected rows
    }
}
