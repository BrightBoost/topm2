package com.example.demos;

import com.example.model.Student;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class Demo2CRUD {
    public static void main(String[] args) {
        System.out.println("=== DEMO 2: CRUD & DIRTY CHECKING ===");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            System.out.println("\n--- PERSISTING 3 STUDENTS ---");
            Student s1 = new Student("Alice", 20, "S001");
            Student s2 = new Student("Bob", 22, "S002");
            Student s3 = new Student("Charlie", 19, "S003");

            session.persist(s1);
            session.persist(s2);
            session.persist(s3);

            transaction.commit();
            System.out.println("3 students saved.");
        }

        Long studentId;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("\n--- RETRIEVING AND MODIFYING (DIRTY CHECKING) ---");
            // Find a student to update
            Student student = session.get(Student.class, 1L);
            studentId = student.getId();
            System.out.println("Retrieved: " + student);

            Transaction transaction = session.beginTransaction();
            
            System.out.println("Updating name to 'Alice Updated' via setter (NO session.update() call)...");
            student.setName("Alice Updated");
            
            System.out.println("Committing transaction. Watch for the UPDATE SQL!");
            transaction.commit(); 
            // Dirty checking: Hibernate detects the change in the managed entity and syncs with DB.
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("\n--- DELETING A STUDENT ---");
            Transaction transaction = session.beginTransaction();
            
            Student student = session.get(Student.class, studentId);
            if (student != null) {
                System.out.println("Deleting student: " + student);
                session.remove(student);
            }
            
            transaction.commit();
        }

        HibernateUtil.shutdown();
        System.out.println("=====================================");
    }
}
