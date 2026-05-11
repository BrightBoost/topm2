package com.example.demos;

import com.example.model.Student;
import com.example.util.DemoData;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class Demo2FirstLevelCache {

    public static void main(String[] args) {
        System.out.println("=== DEMO 2: FIRST-LEVEL CACHE ===\n");

        SessionFactory sessionFactory = HibernateUtil.buildSessionFactory("day5-l1", true, false);
        try {
            DemoData.seed(sessionFactory);
            Long studentId = DemoData.firstStudentId(sessionFactory);

            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();

                System.out.println("1. First find by id. Expect SQL.");
                Student firstLookup = session.find(Student.class, studentId);

                System.out.println("\n2. Second find by the same id. Expect no SQL.");
                Student secondLookup = session.find(Student.class, studentId);
                System.out.println("   same Java instance? " + (firstLookup == secondLookup));

                System.out.println("\n3. Fetch by a different query path. SQL appears, but Hibernate reuses the same managed instance.");
                Student byEmail = session.createQuery(
                                "select s from Student s where s.email = :email", Student.class)
                        .setParameter("email", firstLookup.getEmail())
                        .getSingleResult();
                System.out.println("   same Java instance? " + (firstLookup == byEmail));

                System.out.println("\n4. Modify the managed entity and read it again before commit.");
                firstLookup.setEmail("updated-within-session@school.example");
                Student afterChange = session.find(Student.class, studentId);
                System.out.println("   email after change  : " + afterChange.getEmail());

                transaction.commit();
            }

            try (Session session = sessionFactory.openSession()) {
                System.out.println("\n5. Open a new session. The cache is gone, so SQL appears again.");
                Student reloaded = session.find(Student.class, studentId);
                System.out.println("   email in new session: " + reloaded.getEmail());
            }
        } finally {
            sessionFactory.close();
        }
    }
}