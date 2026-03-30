package com.example.demos;

import com.example.util.HibernateUtil;
import org.hibernate.SessionFactory;

public class Demo1Setup {
    public static void main(String[] args) {
        System.out.println("=== DEMO 1: PROJECT SETUP & ENTITY MAPPING ===");
        System.out.println("Building SessionFactory...");

        // The SessionFactory is built programmatically in HibernateUtil.
        // During this process, Hibernate reads the annotated classes and
        // triggers 'hbm2ddl.auto = create'.
        // Watch the console for the 'CREATE TABLE' SQL statements.
        
        try (SessionFactory sessionFactory = HibernateUtil.getSessionFactory()) {
            if (sessionFactory != null) {
                System.out.println("\nSUCCESS: SessionFactory created and tables auto-generated.");
            } else {
                System.out.println("\nFAILURE: SessionFactory is null.");
            }
        } finally {
            HibernateUtil.shutdown();
        }
        
        System.out.println("===============================================");
    }
}
