package com.example.demos;

import com.example.model.Course;
import com.example.model.Enrollment;
import com.example.model.Student;
import com.example.util.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * DEMO 2: Fetch Strategies en het N+1 Probleem
 *
 * Laat zien:
 * - Het N+1 probleem: 1 query voor studenten + N queries voor enrollments
 * - Fix 1: JOIN FETCH — alles in één query
 * - Fix 2: @BatchSize — enrollments in batches laden
 * - Proxy-inspectie: Hibernate.isInitialized()
 * - LazyInitializationException als de Session gesloten is
 */
public class Demo2FetchStrategies {
    public static void main(String[] args) {
        System.out.println("=== DEMO 2: FETCH STRATEGIES & N+1 PROBLEEM ===\n");

        // Testdata aanmaken
        seedData();
        System.out.println("Testdata aangemaakt: 10 studenten met elk 2-3 enrollments.\n");

        // -------------------------------------------------------
        // STAP 1: Het N+1 probleem demonstreren
        // -------------------------------------------------------
        System.out.println("========================================");
        System.out.println("STAP 1: HET N+1 PROBLEEM");
        System.out.println("========================================");
        System.out.println("We laden alle studenten en benaderen hun enrollments.");
        System.out.println("Tel het aantal SELECT-statements!\n");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Query 1: alle studenten ophalen
            List<Student> students = session.createQuery(
                    "SELECT s FROM Student s", Student.class).getResultList();

            System.out.println(">>> " + students.size() + " studenten opgehaald (1 query)");
            System.out.println(">>> Nu benaderen we de enrollments...\n");

            int queryCount = 1; // we hebben al 1 query gedaan
            for (Student s : students) {
                // Elke keer dat we enrollments benaderen: EXTRA query!
                int size = s.getEnrollments().size();
                queryCount++;
            }

            System.out.println("\n>>> Totaal: " + queryCount + " queries voor " +
                    students.size() + " studenten.");
            System.out.println(">>> Dat is het N+1 probleem: 1 + N = " + queryCount + " queries!");
            System.out.println(">>> Met 1000 studenten: 1001 queries. Dat schaalt niet.\n");
        }

        // -------------------------------------------------------
        // STAP 2: Fix met JOIN FETCH
        // -------------------------------------------------------
        System.out.println("========================================");
        System.out.println("STAP 2: FIX MET JOIN FETCH");
        System.out.println("========================================");
        System.out.println("Eén JPQL query die studenten EN enrollments tegelijk ophaalt.\n");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Student> students = session.createQuery(
                    "SELECT DISTINCT s FROM Student s JOIN FETCH s.enrollments",
                    Student.class).getResultList();

            System.out.println(">>> " + students.size() + " studenten opgehaald MET enrollments.");

            for (Student s : students) {
                // Geen extra query! De enrollments zijn al geladen.
                System.out.println("  " + s.getName() + ": " +
                        s.getEnrollments().size() + " enrollments");
            }

            System.out.println("\n>>> Totaal: 1 query! De JOIN haalt alles tegelijk op.");
            System.out.println(">>> Kijk naar de gegenereerde SQL: je ziet een JOIN.\n");
        }

        // -------------------------------------------------------
        // STAP 3: Proxy-inspectie
        // -------------------------------------------------------
        System.out.println("========================================");
        System.out.println("STAP 3: PROXY-INSPECTIE");
        System.out.println("========================================");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Student s = session.get(Student.class, 1L);

            // De enrollments-collectie is nog niet geladen
            System.out.println("Enrollments geladen? " +
                    Hibernate.isInitialized(s.getEnrollments()));

            // Nu benaderen we de collectie — dit triggert de lazy load
            System.out.println("Aantal enrollments: " + s.getEnrollments().size());

            System.out.println("Enrollments geladen? " +
                    Hibernate.isInitialized(s.getEnrollments()));

            // Proxy op een @ManyToOne
            Enrollment enrollment = s.getEnrollments().get(0);
            Course courseProxy = enrollment.getCourse();
            System.out.println("\nCourse klasse: " + courseProxy.getClass().getName());
            System.out.println("Is dit een proxy? " +
                    (courseProxy.getClass() != Course.class ? "JA" : "NEE"));
            System.out.println("Course geladen? " +
                    Hibernate.isInitialized(courseProxy));

            // Triggeren
            System.out.println("Course naam: " + courseProxy.getName());
            System.out.println("Course geladen? " +
                    Hibernate.isInitialized(courseProxy));
        }

        // -------------------------------------------------------
        // STAP 4: LazyInitializationException
        // -------------------------------------------------------
        System.out.println("\n========================================");
        System.out.println("STAP 4: LazyInitializationException");
        System.out.println("========================================");
        System.out.println("Wat als de Session al dicht is?\n");

        Student detachedStudent;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            detachedStudent = session.get(Student.class, 1L);
            System.out.println("Student opgehaald: " + detachedStudent);
            // Session gaat nu dicht...
        }

        try {
            System.out.println("Session is gesloten. We proberen enrollments te benaderen...");
            int size = detachedStudent.getEnrollments().size();
            System.out.println("Enrollments: " + size); // dit wordt nooit bereikt
        } catch (LazyInitializationException e) {
            System.out.println("\nCAUGHT: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
            System.out.println("\nDit is Hibernate die zegt: 'Ik kan geen data laden want");
            System.out.println("ik heb geen databaseconnectie meer (de Session is dicht).'");
            System.out.println("\nOplossingen:");
            System.out.println("  1. Data ophalen BINNEN de Session (JOIN FETCH)");
            System.out.println("  2. Hibernate.initialize(collection) aanroepen in de Session");
            System.out.println("  3. @BatchSize als vangnet");
        }

        System.out.println("\n=== DEMO 2 KLAAR ===");
        HibernateUtil.shutdown();
    }

    /**
     * Maakt testdata: 3 cursussen en 10 studenten met enrollments.
     */
    private static void seedData() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Course databases = new Course("Databases", 3, "SQL en relationele databases");
            Course webdev = new Course("Web Development", 4, "Frontend en backend");
            Course security = new Course("Security", 3, "Cybersecurity");
            session.persist(databases);
            session.persist(webdev);
            session.persist(security);

            Course[] courses = {databases, webdev, security};

            for (int i = 1; i <= 10; i++) {
                Student student = new Student(
                        "Student " + i,
                        "S" + String.format("%05d", i),
                        "student" + i + "@university.nl"
                );

                student.enroll(courses[0]);
                student.enroll(courses[1]);
                if (i % 2 == 0) {
                    student.enroll(courses[2]);
                }

                session.persist(student);
            }

            tx.commit();
        }
    }
}
