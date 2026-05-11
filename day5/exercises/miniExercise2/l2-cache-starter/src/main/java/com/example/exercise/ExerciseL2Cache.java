package com.example.exercise;

import com.example.model.Course;
import com.example.model.Department;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.hibernate.Transaction;

public class ExerciseL2Cache {

    public static void main(String[] args) {
        System.out.println("=== L2 CACHE EXERCISE ===\n");

        // Part 1: Compare L2 disabled vs. enabled
        System.out.println("PART 1: L2 Cache Comparison");
        compareL2CacheBenefits();

        // Part 2: Invalidation behavior (ORM managed update)
        System.out.println("\n\nPART 2: Invalidation After ORM Update");
        testInvalidationAfterUpdate();

        // Part 3: Staleness risk (native SQL bypass)
        System.out.println("\n\nPART 3: Staleness from Native SQL");
        testStalenessFromNativeSQL();

        System.out.println("\n\nDone!");
    }

    private static void compareL2CacheBenefits() {
        // TODO: Implement L2 cache comparison
        // 1. Build two SessionFactories: one with L2 disabled, one with L2 enabled
        // 2. Seed data in both
        // 3. Perform repeated reads (5 times) of the same course in separate sessions
        // 4. Print statistics (query count, cache hits/misses)
        // 5. Compare the results
        System.out.println("TODO: Implement compareL2CacheBenefits()");
    }

    private static void testInvalidationAfterUpdate() {
        // TODO: Implement ORM update invalidation test
        // 1. Build SessionFactory with L2 enabled
        // 2. Seed data with one course
        // 3. Fetch the course in Session 1 (loads into L2 cache)
        // 4. Update the course name via ORM in Session 2
        // 5. Fetch in Session 3 and verify new name appears (cache was invalidated)
        System.out.println("TODO: Implement testInvalidationAfterUpdate()");
    }

    private static void testStalenessFromNativeSQL() {
        // TODO: Implement native SQL staleness test
        // 1. Build SessionFactory with L2 enabled
        // 2. Seed one course
        // 3. Fetch and cache in Session 1
        // 4. Update via native SQL (bypasses ORM, so cache is not invalidated)
        // 5. Fetch in Session 2 and observe stale value
        // 6. Manually evict from L2 cache and fetch again
        System.out.println("TODO: Implement testStalenessFromNativeSQL()");
    }

    private static void printStatistics(SessionFactory sessionFactory, String label) {
        Statistics stats = sessionFactory.getStatistics();
        System.out.println("\n" + label + ":");
        System.out.println("  Queries executed: " + stats.getQueryExecutionCount());
        System.out.println("  L2 cache hits: " + stats.getSecondLevelCacheHitCount());
        System.out.println("  L2 cache misses: " + stats.getSecondLevelCacheMissCount());
    }
}
