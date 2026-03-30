package com.example.demos;

import com.example.model.Person;
import com.example.model.Student;
import com.example.util.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class Demo5Proxy {
    public static void main(String[] args) {
        System.out.println("=== DEMO 5: THE PROXY PROBLEM ===");

        Long studentId;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Student s = new Student("Proxy Test Student", 20, "S-PROXY");
            session.persist(s);
            transaction.commit();
            studentId = s.getId();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("\n--- GETTING A REFERENCE (LAZY PROXY) ---");
            // getReference (or load in older Hibernate) returns a proxy without hitting the DB
            Person proxy = session.getReference(Person.class, studentId);
            
            System.out.println("Proxy class: " + proxy.getClass().getName());
            
            System.out.println("\n--- TESTING INSTANCEOF ---");
            System.out.println("proxy instanceof Student? " + (proxy instanceof Student));
            System.out.println("Wait... it is a Student in the DB, but why is this false?");
            System.out.println("Answer: The proxy is a dynamically generated subclass of Person, not Student.");

            System.out.println("\n--- UNPROXYING ---");
            Person unproxied = Hibernate.unproxy(proxy, Person.class);
            System.out.println("Unproxied class: " + unproxied.getClass().getName());
            System.out.println("unproxied instanceof Student? " + (unproxied instanceof Student));
        }

        HibernateUtil.shutdown();
        System.out.println("==================================");
    }
}
