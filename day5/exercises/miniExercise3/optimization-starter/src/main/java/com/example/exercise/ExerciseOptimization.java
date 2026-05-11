package com.example.exercise;

import com.example.dto.StudentListRow;
import com.example.model.Course;
import com.example.model.Department;
import com.example.model.Enrollment;
import com.example.model.Student;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.hibernate.Transaction;
import java.time.LocalDate;
import java.util.List;

public class ExerciseOptimization {

    private static final SessionFactory sessionFactory = HibernateUtil.buildSessionFactory("day5-optimization", false);

    public static void main(String[] args) {
        try {
            seedData();

            System.out.println("=== ORM PERFORMANCE OPTIMIZATION ===\n");

            // Part 1: Naive list loading
            System.out.println("PART 1: Naive List Screen (full entities, lazy relationships)");
            testNaiveListScreen();

            // Part 2: Projection-based list loading
            System.out.println("\n\nPART 2: Optimized List Screen (projections)");
            testProjectionListScreen();

            // Part 3: Detail screen with join fetch
            System.out.println("\n\nPART 3: Detail Screen (with join fetch)?");
            testDetailScreenWithJoinFetch();

            // Part 4: Analysis and decision
            System.out.println("\n\nPART 4: Analysis and Recommendations");
            printDecisionFramework();

        } finally {
            sessionFactory.close();
        }
    }

    private static void testNaiveListScreen() {
        // TODO: Implement naive list screen
        // 1. Query all students: SELECT s FROM Student s
        // 2. Loop over each student and access: name, department.name, enrollments.size()
        // 3. Count SQL queries and print
        System.out.println("TODO: Implement testNaiveListScreen()");
    }

    private static void testProjectionListScreen() {
        // TODO: Implement projection-based list screen
        // 1. Use a constructor projection to fetch only: name, email, departmentName
        // Example query: SELECT new com.example.dto.StudentListRow(...) FROM ...
        // 2. Verify that only 1 query is used and fewer columns are fetched
        System.out.println("TODO: Implement testProjectionListScreen()");
    }

    private static void testDetailScreenWithJoinFetch() {
        // TODO: Implement detail screen with join fetch
        // 1. Use JOIN FETCH to load student, department, enrollments, and courses in one query
        // 2. Access all relationships without triggering additional queries
        // 3. Compare statistics with naive approach
        System.out.println("TODO: Implement testDetailScreenWithJoinFetch()");
    }

    private static void printDecisionFramework() {
        System.out.println("Decision Framework for ORM Optimization:");
        System.out.println("1. Use projections for read-only list screens");
        System.out.println("2. Use join fetch for detail screens with full entity graphs");
        System.out.println("3. Measure first — avoid premature optimization");
        System.out.println("4. Choose caching strategy based on data volatility");
        System.out.println("5. Database indexes must support the queries");
    }

    private static void seedData() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            Department cs = new Department("CS", "Computer Science");
            Department business = new Department("BUS", "Business");

            Course java = new Course("Java Fundamentals", 5);
            Course databases = new Course("Database Design", 4);
            Course web = new Course("Web Development", 3);

            cs.getCourses().add(java);
            cs.getCourses().add(databases);
            business.getCourses().add(web);

            for (int i = 1; i <= 5; i++) {
                Student s = new Student("Student " + i, "student" + i + "@uni.local");
                s.setDepartment(cs);
                
                Enrollment e1 = new Enrollment(LocalDate.now(), "A");
                e1.setStudent(s);
                e1.setCourse(java);
                s.getEnrollments().add(e1);
                
                Enrollment e2 = new Enrollment(LocalDate.now(), "B");
                e2.setStudent(s);
                e2.setCourse(databases);
                s.getEnrollments().add(e2);

                cs.getStudents().add(s);
                session.persist(s);
            }

            session.persist(cs);
            session.persist(business);
            tx.commit();
        }
    }

    private static void printStatistics(SessionFactory sessionFactory, String label) {
        Statistics stats = sessionFactory.getStatistics();
        System.out.println("\n[Statistics] " + label + ":");
        System.out.println("  Queries executed: " + stats.getQueryExecutionCount());
        System.out.println("  Collections fetched: " + stats.getCollectionLoadCount());
    }
}
