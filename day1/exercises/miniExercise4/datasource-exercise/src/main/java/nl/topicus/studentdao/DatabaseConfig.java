package nl.topicus.studentdao;

import javax.sql.DataSource;

/**
 * BONUS: Utility class voor database configuratie.
 *
 * TODO: Implementeer de createDataSource() methode die een volledig
 *       geconfigureerde DataSource teruggeeft.
 *
 * Denk na over:
 * - Moet je de configuratiewaarden hardcoden, of kun je ze uit een bestand laden?
 * - Hoe zorg je dat er maar een DataSource-instantie wordt aangemaakt?
 */
public class DatabaseConfig {

    // TODO: Implementeer deze methode
    public static DataSource createDataSource() {
        // Maak een JdbcDataSource aan, configureer hem, en return hem
        throw new UnsupportedOperationException("Nog niet geimplementeerd");
    }

    // EXTRA BONUS: Laad configuratie uit een database.properties bestand
    // Tip: gebruik Properties.load() met een InputStream
}
