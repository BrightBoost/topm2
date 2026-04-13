package com.example.demos;

import com.example.model.Course;
import com.example.model.Enrollment;
import com.example.model.Student;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;

/**
 * DEMO 1: Entity Relationships in Hibernate
 *
 * Laat zien:
 * - @OneToMany / @ManyToOne relaties tussen Student, Course en Enrollment
 * - CascadeType.PERSIST: enrollments worden automatisch meegesaved met de student
 * - Bidirectionele relatie-synchronisatie: beide kanten moeten gezet worden
 * - De gegenereerde SQL: foreign keys op de enrollments-tabel
 * - De helper-methode student.enroll(course)
 */
public class Demo1Relationships {
    public static void main(String[] args) {
        System.out.println("=== DEMO 1: ENTITY RELATIONSHIPS ===\n");

        // -------------------------------------------------------
        // STAP 1: Cursussen aanmaken (geen cascade, apart opslaan)
        // -------------------------------------------------------
        System.out.println("--- STAP 1: Cursussen aanmaken ---");
        System.out.println("Let op de CREATE TABLE statements hierboven.");
        System.out.println("Merk op: enrollments-tabel heeft foreign keys student_id en course_id.\n");

        Course databases, webdev, security;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            databases = new Course("Databases", 3, "SQL en relationele databases");
            webdev = new Course("Web Development", 4, "Frontend en backend development");
            security = new Course("Security", 3, "Cybersecurity fundamentals");

            session.persist(databases);
            session.persist(webdev);
            session.persist(security);

            tx.commit();
            System.out.println("3 cursussen opgeslagen.\n");
        }

        // -------------------------------------------------------
        // STAP 2: Student met enrollments — cascade in actie
        // -------------------------------------------------------
        System.out.println("--- STAP 2: Student met enrollments (CASCADE) ---");
        System.out.println("We persisteren ALLEEN de student.");
        System.out.println("De enrollments worden automatisch opgeslagen dankzij CascadeType.PERSIST.\n");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // Cursussen opnieuw ophalen in deze Session
            databases = session.get(Course.class, databases.getId());
            webdev = session.get(Course.class, webdev.getId());
            security = session.get(Course.class, security.getId());

            // Student aanmaken
            Student alice = new Student("Alice", "S12345", "alice@university.nl");

            // Handmatig: beide kanten van de relatie zetten
            Enrollment e1 = new Enrollment(LocalDate.now(), null);
            e1.setStudent(alice);        // owning side (foreign key)
            e1.setCourse(databases);
            alice.getEnrollments().add(e1); // inverse side (Java object-graph)

            // Met de helper-methode: veel makkelijker!
            alice.enroll(webdev);
            alice.enroll(security);

            // Alleen de student persisteren — enrollments cascaden mee
            session.persist(alice);

            System.out.println("Let op de SQL: eerst INSERT student, dan INSERT enrollments.");
            System.out.println("Hibernate weet dat de student eerst moet bestaan (FK dependency).\n");

            tx.commit();
        }

        // -------------------------------------------------------
        // STAP 3: Verifiëren — data ophalen en relaties tonen
        // -------------------------------------------------------
        System.out.println("--- STAP 3: Data verifiëren ---");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Alle enrollments ophalen
            List<Enrollment> enrollments = session.createQuery(
                    "SELECT e FROM Enrollment e", Enrollment.class).getResultList();

            System.out.println("Alle enrollments in de database:");
            for (Enrollment e : enrollments) {
                System.out.println("  " + e.getStudent().getName()
                        + " → " + e.getCourse().getName()
                        + " (datum: " + e.getEnrollmentDate() + ")");
            }

            // Via de student
            Student alice = session.createQuery(
                    "SELECT s FROM Student s WHERE s.name = :name", Student.class)
                    .setParameter("name", "Alice")
                    .getSingleResult();

            System.out.println("\nAlice's enrollments via de student-kant:");
            for (Enrollment e : alice.getEnrollments()) {
                System.out.println("  → " + e.getCourse().getName());
            }
        }

        // -------------------------------------------------------
        // STAP 4: Wat als je alleen ÉÉN kant van de relatie zet?
        // -------------------------------------------------------
        System.out.println("\n--- STAP 4: Alleen owning side zetten ---");
        System.out.println("Wat als we enrollment.setStudent() vergeten?\n");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Student bob = new Student("Bob", "S67890", "bob@university.nl");
            session.persist(bob);

            Course course = session.get(Course.class, 1L);

            Enrollment broken = new Enrollment(LocalDate.now(), null);
            broken.setCourse(course);
            // FOUT: we zetten enrollment.setStudent() NIET!
            // We voegen alleen toe aan de inverse side:
            bob.getEnrollments().add(broken);

            session.persist(broken);
            tx.commit();

            System.out.println("Enrollment is opgeslagen, maar student_id is NULL in de database!");
            System.out.println("Reden: Hibernate kijkt alleen naar de owning side (@ManyToOne).");
            System.out.println("bob.getEnrollments().add() is de inverse side — die wordt genegeerd.\n");
        }

        // Verifiëren
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> results = session.createNativeQuery(
                    "SELECT id, student_id, course_id FROM enrollments", Object[].class).getResultList();
            System.out.println("Raw database-inhoud (enrollments):");
            for (Object[] row : results) {
                System.out.println("  id=" + row[0] + ", student_id=" + row[1] + ", course_id=" + row[2]);
            }
            System.out.println("^ Let op: de laatste enrollment heeft student_id = NULL!\n");
        }

        System.out.println("=== DEMO 1 KLAAR ===");
        HibernateUtil.shutdown();
    }
}
