package com.example.demos;

import com.example.demos.decorator.*;

/**
 * DEMO 3: Het Decorator Pattern
 *
 * Laat zien:
 * - Een interface (Repository) als component
 * - Een concrete implementatie (SimpleRepository)
 * - Decorators die gedrag toevoegen: Logging, Timing, Caching
 * - Hoe decorators gestapeld worden
 * - Vergelijking met Hibernate proxies
 */
public class Demo3DecoratorPattern {
    public static void main(String[] args) {
        System.out.println("=== DEMO 3: HET DECORATOR PATTERN ===\n");

        // -------------------------------------------------------
        // STAP 1: Zonder decorators — de "kale" repository
        // -------------------------------------------------------
        System.out.println("========================================");
        System.out.println("STAP 1: SIMPELE REPOSITORY (GEEN DECORATORS)");
        System.out.println("========================================\n");

        Repository<Product> simpleRepo = new SimpleRepository();
        simpleRepo.save(new Product(1, "Laptop", 999.99));
        simpleRepo.save(new Product(2, "Muis", 29.99));
        simpleRepo.save(new Product(3, "Toetsenbord", 79.99));

        System.out.println("Opgeslagen: 3 producten");
        System.out.println("findById(1): " + simpleRepo.findById(1).orElse(null));
        System.out.println("findAll: " + simpleRepo.findAll());
        System.out.println("\n>>> Geen extra output — we zien niet wat er gebeurt.\n");

        // -------------------------------------------------------
        // STAP 2: Met LoggingRepository decorator
        // -------------------------------------------------------
        System.out.println("========================================");
        System.out.println("STAP 2: LOGGING DECORATOR");
        System.out.println("========================================");
        System.out.println("We wrappen de SimpleRepository met een LoggingRepository.\n");

        Repository<Product> loggingRepo = new LoggingRepository(new SimpleRepository());

        loggingRepo.save(new Product(1, "Laptop", 999.99));
        loggingRepo.findById(1);
        loggingRepo.delete(1);

        System.out.println("\n>>> De LoggingRepository delegeert naar SimpleRepository.");
        System.out.println(">>> De SimpleRepository weet niet dat hij gewrapped is!\n");

        // -------------------------------------------------------
        // STAP 3: Met TimingRepository decorator
        // -------------------------------------------------------
        System.out.println("========================================");
        System.out.println("STAP 3: TIMING DECORATOR");
        System.out.println("========================================");
        System.out.println("Nu meten we hoe lang elke operatie duurt.\n");

        Repository<Product> timingRepo = new TimingRepository(new SimpleRepository());

        timingRepo.save(new Product(1, "Laptop", 999.99));
        timingRepo.findById(1);
        timingRepo.findAll();

        System.out.println();

        // -------------------------------------------------------
        // STAP 4: Decorators stapelen!
        // -------------------------------------------------------
        System.out.println("========================================");
        System.out.println("STAP 4: DECORATORS STAPELEN");
        System.out.println("========================================");
        System.out.println("Logging + Timing + SimpleRepository.\n");

        Repository<Product> stackedRepo =
                new LoggingRepository(
                        new TimingRepository(
                                new SimpleRepository()));

        stackedRepo.save(new Product(1, "Laptop", 999.99));
        System.out.println();
        stackedRepo.findById(1);

        System.out.println("\n>>> Elke decorator voegt één verantwoordelijkheid toe.");
        System.out.println(">>> Dit is het Single Responsibility Principle in actie.\n");

        // -------------------------------------------------------
        // STAP 5: Caching decorator — cache hit vs. miss
        // -------------------------------------------------------
        System.out.println("========================================");
        System.out.println("STAP 5: CACHING DECORATOR");
        System.out.println("========================================");
        System.out.println("Logging + Timing + Caching + SimpleRepository.\n");

        Repository<Product> cachedRepo =
                new LoggingRepository(
                        new TimingRepository(
                                new CachingRepository(
                                        new SimpleRepository())));

        cachedRepo.save(new Product(1, "Laptop", 999.99));
        System.out.println();

        System.out.println("--- Eerste keer ophalen (MISS): ---");
        cachedRepo.findById(1);
        System.out.println();

        System.out.println("--- Tweede keer ophalen (HIT): ---");
        cachedRepo.findById(1);
        System.out.println();

        System.out.println(">>> Kijk naar het verschil in timing!");
        System.out.println(">>> De cache-hit is veel sneller omdat de SimpleRepository");
        System.out.println(">>> helemaal niet meer wordt aangeroepen.\n");

        // -------------------------------------------------------
        // STAP 6: Vergelijking met Java I/O en Hibernate
        // -------------------------------------------------------
        System.out.println("========================================");
        System.out.println("STAP 6: HET GROTERE PLAATJE");
        System.out.println("========================================");
        System.out.println();
        System.out.println("Decorator pattern in de praktijk:");
        System.out.println();
        System.out.println("  Java I/O (zelfde patroon!):");
        System.out.println("    new BufferedReader(");
        System.out.println("        new InputStreamReader(");
        System.out.println("            new FileInputStream(\"file.txt\")))");
        System.out.println();
        System.out.println("  Onze demo:");
        System.out.println("    new LoggingRepository(");
        System.out.println("        new TimingRepository(");
        System.out.println("            new CachingRepository(");
        System.out.println("                new SimpleRepository())))");
        System.out.println();
        System.out.println("  Hibernate proxy (vergelijkbaar idee):");
        System.out.println("    Proxy extends Student {");
        System.out.println("        // Voegt lazy-loading toe");
        System.out.println("        // Delegeert naar echte Student bij toegang");
        System.out.println("    }");
        System.out.println();
        System.out.println("  Verschil: Hibernate proxies worden dynamisch gegenereerd");
        System.out.println("  via ByteBuddy (subclassing), terwijl onze decorators");
        System.out.println("  handmatig geschreven wrappen via compositie.");
        System.out.println();
        System.out.println("  Decorator vs Inheritance:");
        System.out.println("    Met inheritance zouden we nodig hebben:");
        System.out.println("      LoggingTimingCachingRepository");
        System.out.println("      LoggingTimingRepository");
        System.out.println("      LoggingCachingRepository");
        System.out.println("      TimingCachingRepository");
        System.out.println("      ... etc (class explosion!)");
        System.out.println("    Met decorators: gewoon stapelen naar behoefte.");

        System.out.println("\n=== DEMO 3 KLAAR ===");
    }
}
