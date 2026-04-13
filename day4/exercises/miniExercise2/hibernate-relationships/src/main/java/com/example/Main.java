package com.example;

import com.example.util.HibernateUtil;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== MINI EXERCISE 2: ENTITY RELATIONSHIPS ===\n");

        // -------------------------------------------------------
        // Stap 1: Verifieer dat de tabellen worden aangemaakt
        // -------------------------------------------------------
        // Start de SessionFactory en controleer de console-output.
        // Je zou CREATE TABLE statements moeten zien voor students, courses en enrollments.
        // Let op: de enrollments-tabel moet foreign keys hebben naar students en courses.
        // Als je een join table ziet (bijv. students_enrollments), klopt er iets niet met mappedBy!

        var sessionFactory = HibernateUtil.getSessionFactory();
        System.out.println("SessionFactory aangemaakt!\n");

        // -------------------------------------------------------
        // Stap 2: Cursussen aanmaken en opslaan
        // -------------------------------------------------------
        // TODO: Open een Session en begin een Transaction
        // TODO: Maak 2 Course-objecten aan:
        //   - "Databases", 3 credits, "SQL en relationele databases"
        //   - "Web Development", 4 credits, "Frontend en backend development"
        // TODO: Persist de courses en commit de transactie
        // TIP: Courses moeten apart worden opgeslagen — ze worden NIET gecascade via de student


        // -------------------------------------------------------
        // Stap 3: Student met enrollments aanmaken (cascade)
        // -------------------------------------------------------
        // TODO: Open een nieuwe Session en begin een Transaction
        // TODO: Haal de courses opnieuw op in deze Session (via session.get() met hun IDs)
        // TODO: Maak een Student aan: "Alice", "S12345", "alice@university.nl"
        // TODO: Maak 2 Enrollment-objecten aan met LocalDate.now() als datum
        // TODO: Zet BEIDE kanten van de relatie:
        //   - enrollment.setStudent(alice)   <-- owning side (wordt opgeslagen in DB)
        //   - alice.getEnrollments().add(enrollment)  <-- inverse side (houdt Java-objecten in sync)
        // TODO: Zet ook de course op elke enrollment: enrollment.setCourse(course)
        // TODO: Persist alleen de student — de enrollments moeten meekomen via CascadeType.PERSIST
        // TODO: Commit de transactie


        // -------------------------------------------------------
        // Stap 4: Verifiëren
        // -------------------------------------------------------
        // TODO: Open een nieuwe Session
        // TODO: Query alle enrollments: "SELECT e FROM Enrollment e"
        // TODO: Print voor elke enrollment: student naam + course naam
        // TODO: Controleer dat de foreign keys correct zijn ingevuld


        System.out.println("\n=== KLAAR ===");
        HibernateUtil.shutdown();
    }
}
