package com.example.demos;

import com.example.model.Person;
import com.example.model.Student;
import com.example.model.Teacher;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class Demo4Inheritance {
    public static void main(String[] args) {
        System.out.println("=== DEMO 4: INHERITANCE MAPPING (SINGLE TABLE) ===");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            System.out.println("\n--- PERSISTING A MIX OF STUDENTS AND TEACHERS ---");
            session.persist(new Student("John Student", 20, "S999"));
            session.persist(new Teacher("Mary Teacher", 45, "Computer Science"));
            session.persist(new Student("Bob Student", 21, "S888"));

            transaction.commit();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("\n--- QUERYING FOR ALL PERSONS ---");
            // Polymorphic query: querying for the base class returns all subtypes
            List<Person> persons = session.createQuery("FROM Person", Person.class).getResultList();

            for (Person p : persons) {
                System.out.println("Found " + p.getClass().getSimpleName() + ": " + p);
            }

            System.out.println("\n--- OBSERVING DISCRIMINATOR VALUES ---");
            // We can use a native query to see the raw table content including the discriminator
            List<Object[]> rawResults = session.createNativeQuery("SELECT id, name, person_type FROM persons", Object[].class).getResultList();
            for (Object[] row : rawResults) {
                System.out.println("ID: " + row[0] + ", Name: " + row[1] + ", Type (Discriminator): " + row[2]);
            }
        }

        HibernateUtil.shutdown();
        System.out.println("==================================================");
    }
}
