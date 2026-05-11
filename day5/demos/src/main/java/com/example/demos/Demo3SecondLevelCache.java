package com.example.demos;

import com.example.model.Student;
import com.example.util.DemoData;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class Demo3SecondLevelCache {

    public static void main(String[] args) {
        System.out.println("=== DEMO 3: SECOND-LEVEL CACHE ===\n");

        runRepeatedReadComparison();
        runInvalidationAndStalenessExample();
    }

    private static void runRepeatedReadComparison() {
        SessionFactory withoutL2 = HibernateUtil.buildSessionFactory("day5-no-l2", false, false);
        try {
            DemoData.seed(withoutL2);
            Long studentId = DemoData.firstStudentId(withoutL2);

            System.out.println("Repeated reads across separate sessions with L2 disabled:");
            DemoData.resetStatistics(withoutL2);
            runRepeatedReads(withoutL2, studentId, 5);
            DemoData.printStatistics(withoutL2, "Without L2 cache");
        } finally {
            withoutL2.close();
        }

        SessionFactory withL2 = HibernateUtil.buildSessionFactory("day5-with-l2", false, true);
        try {
            DemoData.seed(withL2);
            Long studentId = DemoData.firstStudentId(withL2);

            System.out.println("Repeated reads across separate sessions with L2 enabled:");
            DemoData.resetStatistics(withL2);
            runRepeatedReads(withL2, studentId, 5);
            DemoData.printStatistics(withL2, "With L2 cache");
        } finally {
            withL2.close();
        }
    }

    private static void runInvalidationAndStalenessExample() {
        SessionFactory sessionFactory = HibernateUtil.buildSessionFactory("day5-l2-stale", false, true);
        try {
            DemoData.seed(sessionFactory);
            Long studentId = DemoData.firstStudentId(sessionFactory);

            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                Student student = session.find(Student.class, studentId);
                student.setEmail("hibernate-update@school.example");
                transaction.commit();
            }

            try (Session session = sessionFactory.openSession()) {
                Student refreshed = session.find(Student.class, studentId);
                System.out.println("After ORM update, the cache stays coherent: " + refreshed.getEmail());
            }

            DemoData.updateStudentEmailViaJdbc(sessionFactory, studentId, "native-sql-update@school.example");

            try (Session session = sessionFactory.openSession()) {
                Student stale = session.find(Student.class, studentId);
                System.out.println("After native SQL without eviction: " + stale.getEmail());
            }

            sessionFactory.getCache().evictEntityData(Student.class, studentId);

            try (Session session = sessionFactory.openSession()) {
                Student fresh = session.find(Student.class, studentId);
                System.out.println("After manual eviction: " + fresh.getEmail());
            }
        } finally {
            sessionFactory.close();
        }
    }

    private static void runRepeatedReads(SessionFactory sessionFactory, Long studentId, int repetitions) {
        for (int i = 1; i <= repetitions; i++) {
            try (Session session = sessionFactory.openSession()) {
                Student student = session.find(Student.class, studentId);
                System.out.println("  request " + i + " -> " + student.getName());
            }
        }
        System.out.println();
    }
}