package com.example;

import com.example.util.HibernateUtil;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Hibernate CRUD exercises...");

        var sessionFactory = HibernateUtil.getSessionFactory();
        System.out.println("SessionFactory created. De courses-tabel is aangemaakt.");
        System.out.println();

        // TODO Part 1: Maak 3 cursussen aan met session.persist()

        // TODO Part 2: Haal een cursus op met session.get()

        // TODO Part 3: Wijzig een cursusnaam (alleen setter!) en commit — observeer dirty checking

        // TODO Part 4: Verwijder een cursus en schrijf een JPQL query

        // TODO Part 5 (Bonus): Experimenteer met detached objecten

        HibernateUtil.shutdown();
    }
}
