package com.example.demos;

import java.util.HashMap;
import java.util.Map;

public class Demo1WhyCachingMatters {

    public static void main(String[] args) {
        System.out.println("=== DEMO 1: WHY CACHING MATTERS ===\n");

        SlowStudentRepository uncachedRepository = new SlowStudentRepository();
        runScenario("Without cache", uncachedRepository);

        SlowStudentRepository slowRepository = new SlowStudentRepository();
        CachingStudentRepository cachedRepository = new CachingStudentRepository(slowRepository);
        runScenario("With a small local cache", cachedRepository);

        System.out.println("Key point: caching is not only about speed.");
        System.out.println("It also gives the application one coherent in-memory object for the same row.\n");
    }

    private static void runScenario(String label, StudentRepository repository) {
        long started = System.currentTimeMillis();
        StudentCard first = repository.findById(1L);
        StudentCard second = repository.findById(1L);
        StudentCard third = repository.findById(1L);
        long elapsed = System.currentTimeMillis() - started;

        System.out.println(label);
        System.out.println("  lookups completed in : " + elapsed + " ms");
        System.out.println("  first == second      : " + (first == second));
        System.out.println("  second == third      : " + (second == third));
        System.out.println("  resulting object     : " + third);
        System.out.println();
    }

    private interface StudentRepository {
        StudentCard findById(Long id);
    }

    private record StudentCard(Long id, String name, String email) {
    }

    private static final class SlowStudentRepository implements StudentRepository {
        private final Map<Long, StudentCard> store = Map.of(
                1L, new StudentCard(1L, "Ada Lovelace", "ada@school.example")
        );

        @Override
        public StudentCard findById(Long id) {
            try {
                Thread.sleep(150);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Demo interrupted", exception);
            }

            StudentCard card = store.get(id);
            return new StudentCard(card.id(), card.name(), card.email());
        }
    }

    private static final class CachingStudentRepository implements StudentRepository {
        private final StudentRepository delegate;
        private final Map<Long, StudentCard> cache = new HashMap<>();

        private CachingStudentRepository(StudentRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public StudentCard findById(Long id) {
            return cache.computeIfAbsent(id, delegate::findById);
        }
    }
}