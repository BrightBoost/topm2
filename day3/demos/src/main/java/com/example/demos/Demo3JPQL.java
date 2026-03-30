package com.example.demos;

import com.example.model.Student;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class Demo3JPQL {
    public static void main(String[] args) {
        System.out.println("=== DEMO 3: JPQL ===");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            System.out.println("\n--- PERSISTING STUDENTS WITH VARYING AGES ---");
            session.persist(new Student("David", 25, "S101"));
            session.persist(new Student("Eve", 18, "S102"));
            session.persist(new Student("Frank", 30, "S103"));
            session.persist(new Student("Grace", 22, "S104"));

            transaction.commit();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("\n--- RUNNING JPQL QUERY ---");
            int minAge = 21;
            String jpql = "SELECT s FROM Student s WHERE s.age > :minAge";
            
            System.out.println("JPQL Query: " + jpql);
            System.out.println("Executing with minAge = " + minAge);
            System.out.println("Watch for the generated SQL!");

            Query<Student> query = session.createQuery(jpql, Student.class);
            query.setParameter("minAge", minAge);

            List<Student> results = query.getResultList();

            System.out.println("\nRESULTS:");
            results.forEach(System.out::println);
        }

        HibernateUtil.shutdown();
        System.out.println("======================");
    }
}
