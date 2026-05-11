package com.example.exercise;

import com.example.model.Course;
import com.example.model.Department;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class ExerciseL1Cache {

    private static final SessionFactory sessionFactory = HibernateUtil.buildSessionFactory("day5-l1", true);

    public static void main(String[] args) {
        try {
            // Seed some test data
            seedData();

            // Part 1: Observe L1 cache in action
            System.out.println("=== PART 1: L1 Cache Behavior ===\n");
            // TODO: Implement part 1
            // 1a. Get the first course ID
            // 1b. Open a session and find the course (expect a SELECT)
            // 1c. Find the same course again (expect no SELECT — L1 cache hit)
            // 1d. Check if both references point to the same Java object (use ==)

            // Part 2: Identity guarantee
            System.out.println("\n=== PART 2: Identity Guarantee ===\n");
            // TODO: Implement part 2
            // 2a. In the same session, find a course and fetch it by name
            // 2b. Verify that both paths return the same Java instance

            // Part 3: New session, cache is gone
            System.out.println("\n=== PART 3: New Session, Cache Reset ===\n");
            // TODO: Implement part 3
            // 3a. Open a new session and fetch the same course
            // 3b. Verify that the SQL query appears again (L1 is per-session only)

            // Part 4: Detached entities
            System.out.println("\n=== PART 4: Detached Entities ===\n");
            // TODO: Implement part 4
            // 4a. Fetch a course in one session
            // 4b. Close the session (course becomes detached)
            // 4c. Modify the course object outside the session
            // 4d. Open a new session, persist it, and verify the changes held

        } finally {
            sessionFactory.close();
        }
    }

    private static void seedData() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Department cs = new Department("CS", "Computer Science");
            session.persist(cs);

            Course java = new Course("Java Fundamentals", 5);
            Course databases = new Course("Database Design", 4);
            Course web = new Course("Web Development", 3);

            session.persist(java);
            session.persist(databases);
            session.persist(web);

            tx.commit();
        }
    }
}
