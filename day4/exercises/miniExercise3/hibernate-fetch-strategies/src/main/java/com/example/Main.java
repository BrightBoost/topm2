package com.example;

import com.example.model.Course;
import com.example.model.Student;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== MINI EXERCISE 3: FETCH STRATEGIES ===\n");

        // -------------------------------------------------------
        // Stap 0: Testdata aanmaken
        // -------------------------------------------------------
        seedData();
        System.out.println("Testdata aangemaakt: 5 studenten met elk 2-3 enrollments.\n");

        // -------------------------------------------------------
        // Part 1: Het N+1 probleem observeren
        // -------------------------------------------------------
        // TODO: Haal alle studenten op met: "SELECT s FROM Student s"
        // TODO: Loop over elke student en print het aantal enrollments: s.getEnrollments().size()
        // TODO: Tel het aantal SELECT-statements in de console-output
        // VERWACHT: 1 query voor studenten + 5 queries voor enrollments = 6 queries (N+1!)
        System.out.println("=== PART 1: N+1 PROBLEEM ===");


        // -------------------------------------------------------
        // Part 2: Oplossing met JOIN FETCH
        // -------------------------------------------------------
        // TODO: Schrijf een JPQL query: "SELECT DISTINCT s FROM Student s JOIN FETCH s.enrollments"
        // TODO: Loop weer over de studenten en print enrollments
        // TODO: Tel de queries opnieuw — het zou nu 1 moeten zijn
        System.out.println("\n=== PART 2: JOIN FETCH ===");


        // -------------------------------------------------------
        // Part 3: Oplossing met @BatchSize
        // -------------------------------------------------------
        // TODO: Verwijder de JOIN FETCH query en gebruik weer "SELECT s FROM Student s"
        // TODO: Voeg @BatchSize(size = 5) toe aan de enrollments-collectie in Student.java:
        //       @BatchSize(size = 5)
        //       @OneToMany(mappedBy = "student", cascade = CascadeType.PERSIST)
        //       private List<Enrollment> enrollments = new ArrayList<>();
        //   (importeer: org.hibernate.annotations.BatchSize)
        // TODO: Draai dezelfde code en tel de queries
        // VERWACHT: 1 query voor studenten + 1 query met IN-clause = 2 queries
        // LET OP: Zoek in de SQL naar "WHERE student_id IN (?, ?, ?, ?, ?)"
        System.out.println("\n=== PART 3: @BatchSize ===");


        // -------------------------------------------------------
        // Part 4: Eager vs Lazy experiment
        // -------------------------------------------------------
        // TODO: Ga naar Enrollment.java en wijzig @ManyToOne naar @ManyToOne(fetch = FetchType.LAZY)
        //       bij het course-veld
        // TODO: Haal een enrollment op met session.get(Enrollment.class, 1L)
        // TODO: Bekijk de SQL — de course is nog NIET geladen
        // TODO: Roep enrollment.getCourse().getName() aan — nu verschijnt een extra SELECT
        // TODO: Zet het terug naar EAGER (of verwijder fetch = ...) en vergelijk
        System.out.println("\n=== PART 4: EAGER vs LAZY ===");


        // -------------------------------------------------------
        // Bonus: LazyInitializationException
        // -------------------------------------------------------
        // TODO: Haal studenten op in een Session, sluit de Session, en probeer
        //       dan enrollments te benaderen buiten de Session
        // VERWACHT: LazyInitializationException!
        // TODO: Vang de exception op en print de melding
        // TODO: Bedenk: hoe voorkom je dit in een echte applicatie?
        System.out.println("\n=== BONUS: LazyInitializationException ===");


        System.out.println("\n=== KLAAR ===");
        HibernateUtil.shutdown();
    }

    /**
     * Maakt testdata aan: 3 cursussen en 5 studenten,
     * elk ingeschreven voor 2-3 cursussen.
     */
    private static void seedData() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Course databases = new Course("Databases", 3, "SQL en relationele databases");
            Course webdev = new Course("Web Development", 4, "Frontend en backend");
            Course security = new Course("Security", 3, "Cybersecurity fundamentals");
            session.persist(databases);
            session.persist(webdev);
            session.persist(security);

            Course[] courses = {databases, webdev, security};

            for (int i = 1; i <= 5; i++) {
                Student student = new Student(
                        "Student " + i,
                        "S" + String.format("%05d", i),
                        "student" + i + "@university.nl"
                );

                // Elke student krijgt 2-3 cursussen
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
