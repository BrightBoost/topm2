package com.example.demos;

import com.example.model.Department;
import com.example.model.Student;
import com.example.util.DemoData;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class Demo4CachingPitfalls {

    public static void main(String[] args) {
        System.out.println("=== DEMO 4: CACHING PITFALLS ===\n");

        SessionFactory sessionFactory = HibernateUtil.buildSessionFactory("day5-pitfalls", false, false);
        try {
            DemoData.seed(sessionFactory);

            demonstrateMultipleBagFetch(sessionFactory);
            demonstrateOverfetching(sessionFactory);
        } finally {
            sessionFactory.close();
        }
    }

    private static void demonstrateMultipleBagFetch(SessionFactory sessionFactory) {
        System.out.println("1. Join-fetching two bag collections from Department.");
        System.out.println("   This is the classic row explosion / multiple-bag pitfall.\n");

        try (Session session = sessionFactory.openSession()) {
            session.createQuery(
                    "select distinct d from Department d " +
                            "join fetch d.students " +
                            "join fetch d.courses",
                    Department.class
            ).getResultList();

            System.out.println("   Query succeeded unexpectedly. If that happens, inspect the generated row count closely.");
        } catch (Exception exception) {
            Throwable rootCause = rootCause(exception);
            System.out.println("   Caught: " + rootCause.getClass().getSimpleName());
            System.out.println("   Message: " + rootCause.getMessage());
        }

        System.out.println();
    }

    private static void demonstrateOverfetching(SessionFactory sessionFactory) {
        System.out.println("2. Loading a full graph for a tiny screen.");
        System.out.println("   Imagine the UI only needs student name + email, but we load enrollments and courses too.\n");

        DemoData.resetStatistics(sessionFactory);

        try (Session session = sessionFactory.openSession()) {
            List<Student> students = session.createQuery(
                    "select distinct s from Student s " +
                            "left join fetch s.department " +
                            "left join fetch s.enrollments e " +
                            "left join fetch e.course",
                    Student.class
            ).getResultList();

            System.out.println("   Students returned     : " + students.size());
            System.out.println("   First row for the UI  : " + students.getFirst().getName() + " / " + students.getFirst().getEmail());
            System.out.println("   But we also loaded    : " + students.getFirst().getEnrollments().size() + " enrollments for that student");
        }

        DemoData.printStatistics(sessionFactory, "Overfetching snapshot");
        System.out.println("Next section shows the fix: pick a query shape that matches the use case.\n");
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}