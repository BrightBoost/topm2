package com.example;

import com.example.util.HibernateUtil;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Hibernate Polymorphism exercises...");

        var sessionFactory = HibernateUtil.getSessionFactory();
        System.out.println("SessionFactory created.");
        System.out.println();

        // TODO Part 1: Voeg inheritance-annotaties toe aan Course en maak OnlineCourse + ClassroomCourse

        // TODO Part 2: Sla 2 online en 2 klassikale cursussen op, haal ze op als List<Course>

        // TODO Part 3: Provoceer het proxy-probleem met session.getReference() en fix het met Hibernate.unproxy()

        // TODO Part 4 (Bonus): Maak een Department entity en provoceer LazyInitializationException

        HibernateUtil.shutdown();
    }
}
