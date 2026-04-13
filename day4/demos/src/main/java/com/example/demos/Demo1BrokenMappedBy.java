package com.example.demos;

import com.example.model.Enrollment;
import com.example.model.StudentBroken;
import com.example.model.Course;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;

/**
 * DEMO 1b: Wat gebeurt er als je mappedBy vergeet?
 *
 * StudentBroken heeft @OneToMany ZONDER mappedBy.
 * Hibernate maakt een extra join table aan: students_broken_enrollments.
 * Dat is bijna nooit wat je wilt bij een one-to-many relatie.
 */
public class Demo1BrokenMappedBy {
    public static void main(String[] args) {
        System.out.println("=== DEMO 1b: ZONDER mappedBy — DE JOIN TABLE FOUT ===\n");

        System.out.println("Kijk naar de CREATE TABLE statements hierboven.");
        System.out.println("Je ziet nu een EXTRA tabel: students_broken_enrollments.");
        System.out.println("Die tabel heb je niet nodig — de foreign key hoort in enrollments!\n");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Course course = new Course("Test", 3, "Test course");
            session.persist(course);

            StudentBroken broken = new StudentBroken("Broken Student");

            Enrollment e = new Enrollment(LocalDate.now(), null);
            e.setCourse(course);
            // Merk op: we kunnen enrollment.setStudent() niet aanroepen
            // met StudentBroken — de Enrollment kent alleen Student.
            broken.getEnrollments().add(e);

            session.persist(e);
            session.persist(broken);

            tx.commit();
        }

        System.out.println("\n--- Resultaat ---");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Laat zien dat de join table bestaat
            List<Object[]> joinTableRows = session.createNativeQuery(
                    "SELECT * FROM students_broken_enrollments", Object[].class).getResultList();
            System.out.println("Join table students_broken_enrollments bevat " +
                    joinTableRows.size() + " rij(en).");
            System.out.println("Dit is een extra tabel die je niet wilt!");
            System.out.println("\nOplossing: voeg mappedBy = \"student\" toe aan @OneToMany.");
            System.out.println("Dan gebruikt Hibernate de bestaande foreign key in de enrollments-tabel.\n");
        }

        System.out.println("=== DEMO 1b KLAAR ===");
        HibernateUtil.shutdown();
    }
}
