package nl.topicus.studentdao;

/**
 * Hoofdklasse voor de DataSource oefening.
 *
 * Jouw taak: vervang het gebruik van DriverManager door een DataSource.
 */
public class DataSourceApp {

    private static final String DB_URL = "jdbc:h2:file:./data/studentdb";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {

        // TODO: Maak een JdbcDataSource aan (org.h2.jdbcx.JdbcDataSource)
        // TODO: Configureer de URL, user en password op de DataSource
        // TODO: Test de connectie door een Connection te openen en weer te sluiten

        // TODO: Maak een JdbcStudentDao aan met de DataSource (in plaats van url/user/password)

        // Zodra de DAO werkt met de DataSource, test alle operaties:

        // TODO: Tabel aanmaken
        // TODO: Studenten opslaan
        // TODO: Alle studenten ophalen en printen
        // TODO: Een student zoeken op ID
        // TODO: Een student bijwerken
        // TODO: Een student verwijderen
        // TODO: Verifieer het eindresultaat met findAll()
    }
}
