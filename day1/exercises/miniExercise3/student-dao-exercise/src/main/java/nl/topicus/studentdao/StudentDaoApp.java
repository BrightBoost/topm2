package nl.topicus.studentdao;

/**
 * Hoofdklasse voor de Student DAO oefening.
 *
 * Hier test je al je DAO-methodes en kun je (optioneel)
 * een interactieve console-applicatie bouwen.
 */
public class StudentDaoApp {

    private static final String DB_URL = "jdbc:h2:file:./data/studentdb";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        JdbcStudentDao dao = new JdbcStudentDao(DB_URL, DB_USER, DB_PASSWORD);

        // Stap 1: Maak de tabel aan
        dao.createTable();
        System.out.println("Tabel aangemaakt (of bestaat al).");

        // TODO: Stap 2 - Maak een paar Student objecten aan en sla ze op met dao.save()

        // TODO: Stap 3 - Haal alle studenten op met dao.findAll() en print ze

        // TODO: Stap 4 - Zoek een student op ID met dao.findById() en print het resultaat

        // TODO: Stap 5 - Werk een student bij met dao.update() en verifieer de wijziging

        // TODO: Stap 6 - Verwijder een student met dao.delete() en verifieer dat die weg is

        // BONUS TODO: Voeg een interactieve console-loop toe (Scanner)
        //             zodat je via het toetsenbord studenten kunt toevoegen, bekijken, wijzigen en verwijderen
    }
}
