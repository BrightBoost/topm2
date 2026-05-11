package com.example.util;

import com.example.model.Course;
import com.example.model.Department;
import com.example.model.Student;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.stat.Statistics;

import java.sql.PreparedStatement;
import java.util.List;

public final class DemoData {

    private DemoData() {
    }

    public static void seed(SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            Department computerScience = new Department("CS", "Computer Science");
            Department business = new Department("BUS", "Business IT");

            Course orm = new Course("ORM and Hibernate", 5);
            Course performance = new Course("Performance Engineering", 5);
            Course databases = new Course("Databases", 4);
            Course analytics = new Course("Analytics", 4);

            computerScience.addCourse(orm);
            computerScience.addCourse(performance);
            business.addCourse(databases);
            business.addCourse(analytics);

            for (int i = 1; i <= 6; i++) {
                Student student = new Student("Ada Student " + i, "ada" + i + "@school.example");
                computerScience.addStudent(student);
                student.enroll(orm, i % 2 == 0 ? "A" : "B");
                student.enroll(performance, "A");
                session.persist(student);
            }

            for (int i = 1; i <= 6; i++) {
                Student student = new Student("Bob Student " + i, "bob" + i + "@school.example");
                business.addStudent(student);
                student.enroll(databases, i % 2 == 0 ? "A" : "C");
                student.enroll(analytics, "B");
                session.persist(student);
            }

            session.persist(computerScience);
            session.persist(business);

            transaction.commit();
        }
    }

    public static Long firstStudentId(SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("select s.id from Student s order by s.id", Long.class)
                    .setMaxResults(1)
                    .getSingleResult();
        }
    }

    public static void resetStatistics(SessionFactory sessionFactory) {
        sessionFactory.getStatistics().clear();
    }

    public static void printStatistics(SessionFactory sessionFactory, String label) {
        Statistics statistics = sessionFactory.getStatistics();
        System.out.println(label);
        System.out.println("  queries executed     : " + statistics.getQueryExecutionCount());
        System.out.println("  entities loaded      : " + statistics.getEntityLoadCount());
        System.out.println("  second-level hits    : " + statistics.getSecondLevelCacheHitCount());
        System.out.println("  second-level misses  : " + statistics.getSecondLevelCacheMissCount());
        System.out.println("  second-level puts    : " + statistics.getSecondLevelCachePutCount());
        System.out.println();
    }

    public static void updateStudentEmailViaJdbc(SessionFactory sessionFactory, Long studentId, String email) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.doWork(connection -> {
                try (PreparedStatement statement = connection.prepareStatement(
                        "update students set email = ? where id = ?")) {
                    statement.setString(1, email);
                    statement.setLong(2, studentId);
                    statement.executeUpdate();
                }
            });
            transaction.commit();
        }
    }

    public static List<Long> firstStudentIds(SessionFactory sessionFactory, int limit) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("select s.id from Student s order by s.id", Long.class)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }
}