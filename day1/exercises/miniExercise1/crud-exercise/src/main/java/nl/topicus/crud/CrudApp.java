package nl.topicus.crud;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CrudApp {

    // JDBC configuratie voor H2 file-based database
    private static final String URL = "jdbc:h2:file:./data/studentdb";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Verbonden met de database!");

            // Part 1: Tabel aanmaken
            // TODO: Maak de students tabel aan (id, name, email, age)

            // Part 2: Studenten toevoegen
            // TODO: Voeg 3 studenten toe met INSERT statements
            // TODO: Print het aantal affected rows na elke INSERT

            // Part 3: Alle studenten ophalen
            // TODO: Haal alle studenten op met SELECT en print ze

            // Part 4: Student updaten
            // TODO: Update de leeftijd van een student
            // TODO: Print affected rows en haal alle studenten opnieuw op

            // Part 5: Student verwijderen
            // TODO: Verwijder een student
            // TODO: Print affected rows en haal alle studenten opnieuw op

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
