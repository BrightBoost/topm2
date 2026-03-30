package com.example.demos;

import com.example.model.Department;
import com.example.model.Teacher;
import com.example.util.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class Demo6LazyInit {
    public static void main(String[] args) {
        System.out.println("=== DEMO 6: LazyInitializationException ===");

        Long deptId;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Department dept = new Department("CS Department");
            Teacher t1 = new Teacher("Teacher A", 40, "CS");
            Teacher t2 = new Teacher("Teacher B", 35, "CS");
            t1.setDepartment(dept);
            t2.setDepartment(dept);
            
            session.persist(dept);
            session.persist(t1);
            session.persist(t2);
            transaction.commit();
            deptId = dept.getId();
        }

        System.out.println("\n--- TRIGGERING LazyInitializationException ---");
        Department loadedDept;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            loadedDept = session.get(Department.class, deptId);
            System.out.println("Loaded department: " + loadedDept);
            // Session closes after this block.
        }

        try {
            System.out.println("Attempting to access lazy teachers collection outside the session...");
            System.out.println("Number of teachers: " + loadedDept.getTeachers().size());
        } catch (LazyInitializationException e) {
            System.out.println("\nCAUGHT EXPECTED EXCEPTION: " + e.getClass().getSimpleName());
            System.out.println("EXPLANATION: Hibernate cannot load a lazy collection once the Session that loaded its parent entity is closed.");
            System.out.println("The 'teachers' list is just a proxy, and it needs an active database connection to fetch its contents.");
        }

        System.out.println("\n--- FIX 1: ACCESSING INSIDE THE SESSION ---");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Department dept = session.get(Department.class, deptId);
            System.out.println("Department: " + dept);
            // Accessing the collection triggers the load
            System.out.println("Number of teachers (inside session): " + dept.getTeachers().size());
        }

        System.out.println("\n--- FIX 2: FETCH JOIN (JPQL) ---");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // JOIN FETCH loads the parent and children in a single SQL query
            Department dept = session.createQuery(
                    "SELECT d FROM Department d LEFT JOIN FETCH d.teachers WHERE d.id = :id", Department.class)
                    .setParameter("id", deptId)
                    .getSingleResult();
            
            session.close(); // Explicitly close to prove it works
            System.out.println("Department loaded with FETCH JOIN: " + dept);
            System.out.println("Number of teachers (session closed): " + dept.getTeachers().size());
        }

        HibernateUtil.shutdown();
        System.out.println("==========================================");
    }
}
