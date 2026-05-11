# Mini Exercise 2: Second-Level Cache — Spanning Sessions and Invalidation Strategy

## Scenario / Context

Your application is successful and the traffic is growing. You notice that the database is under load. The same students and courses are being looked up repeatedly across different requests. While the first-level cache helped within each request, students from other requests still hit the database. You need a second-level cache: a shared, session-independent cache managed by Hibernate. But you'll have to be aware of stale data. If you update a course via one channel and query it via another without proper invalidation, users would see contradictions. In this exercise, you'll enable L2 cache, observe the benefits, and understand the invalidation strategies that keep your data coherent.

---

## Learning Goals

- Configure Hibernate for second-level caching with a JCache provider (Caffeine)
- Compare repeated reads with L2 disabled (many queries) versus enabled (cache hits)
- Verify that L2 cache hits are measured in statistics (hit count, miss count)
- Demonstrate ORM-managed invalidation: updates flow through Hibernate invalidate the L2 entry
- Reproduce staleness: native SQL updates bypass the ORM and leave stale data in L2 cache
- Apply manual cache eviction (`sessionFactory.getCache().evictEntity()`) to restore coherence
- Evaluate the trade-off between cache scope and correctness guarantees

---

# Lab Parts

This lab contains **3 parts**.

---

## Part 1: Enable L2 Cache and Compare Query Volume

### What you will do

Build two `SessionFactory` instances: one with L2 cache disabled, one with L2 cache enabled. Seed test data in both. Perform the same operation on both by fetching the same course 5 times in 5 separate sessions. Count and compare the SQL queries and cache statistics.

### Success criteria

- With L2 disabled: 5 queries (one per session fetch)
- With L2 enabled: 1 query (first fetch) + 4 cache hits (subsequent sessions)
- Statistics show clear differences in `getQueryExecutionCount()` and `getSecondLevelCacheHitCount()`
- New sessions don't reset the L2 cache; the cache persists across sessions

### Hints

<details>
<summary>Hint 1</summary>

Uncomment the L2 cache configuration in `HibernateUtil.buildSessionFactory()`:

```java
if (enableL2Cache) {
    settings.put(Environment.USE_SECOND_LEVEL_CACHE, "true");
    settings.put("hibernate.cache.region.factory_class", 
        "org.hibernate.cache.jcache.internal.JCacheRegionFactory");
    settings.put("hibernate.javax.cache.provider", 
        "com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider");
    settings.put("hibernate.javax.cache.missing_cache_strategy", "create");
}
```

</details>

<details>
<summary>Hint 2</summary>

Add `@Cacheable` and `@Cache` annotations to your entities:

```java
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "courses")
public class Course { ... }
```

Import from `jakarta.persistence.Cacheable` and `org.hibernate.annotations.Cache`.

</details>

<details>
<summary>Hint 3</summary>

To get the first course ID:

```java
try (Session session = sessionFactory.openSession()) {
    Long courseId = session.createQuery(
        "select c.id from Course c order by c.id", Long.class)
        .setMaxResults(1)
        .getSingleResult();
    return courseId;
}
```

</details>

<details>
<summary>Hint 4</summary>

Repeated reads pattern:

```java
long courseId = getFirstCourseId(sessionFactory);

for (int i = 1; i <= 5; i++) {
    try (Session session = sessionFactory.openSession()) {
        Course course = session.find(Course.class, courseId);
        System.out.println("Request " + i + ": " + course.getName());
    }
}
```

</details>

<details>
<summary>Hint 5</summary>

Use `printStatistics()` helper to display results:

```java
Statistics stats = sessionFactory.getStatistics();
System.out.println("Queries: " + stats.getQueryExecutionCount());
System.out.println("L2 hits: " + stats.getSecondLevelCacheHitCount());
System.out.println("L2 misses: " + stats.getSecondLevelCacheMissCount());
```

</details>

---

## Part 2: Verify ORM-Managed Invalidation

### What you will do

With L2 cache enabled, fetch a course and cache it (Session 1). Update the same course's name via Hibernate (Session 2). Fetch again in a new session (Session 3). Verify that the new name appears (L2 cache was invalidated by the ORM update).

### Success criteria

- Session 1 fetches course X, caches it in L2
- Session 2 updates course X via `session.merge()` and `flush()`
- Session 3 fetches course X and sees the updated name (not stale data)
- Statistics show L2 miss (or new query) in Session 3 after the update
- Conclusion: ORM updates invalidate L2 cache correctly

### Hints

<details>
<summary>Hint 1</summary>

Seed one course first in a separate method, then run the three-session test.

</details>

<details>
<summary>Hint 2</summary>

Session 1:

```java
try (Session session = sessionFactory.openSession()) {
    Course course = session.find(Course.class, courseId);
    originalName = course.getName();
    System.out.println("Session 1: " + course.getName());
}
```

</details>

<details>
<summary>Hint 3</summary>

Session 2 (update via ORM):

```java
try (Session session = sessionFactory.openSession()) {
    Transaction tx = session.beginTransaction();
    Course course = session.find(Course.class, courseId);
    course.setName(originalName + " [Updated]");
    tx.commit(); // This triggers the ORM update and L2 cache invalidation
}
```

</details>

<details>
<summary>Hint 4</summary>

Session 3 (fetch after update):

```java
try (Session session = sessionFactory.openSession()) {
    Course course = session.find(Course.class, courseId);
    System.out.println("Session 3: " + course.getName()); // Should see [Updated]
}
```

Expected output:

```
Session 1: Databases
Session 2: (update to "Databases [Updated]")
Session 3: Databases [Updated]

Statistics show: L2 miss and 1 new query in Session 3
```

</details>

---

## Part 3: Demonstrate Staleness from Native SQL and Manual Eviction

### What you will do

With L2 cache enabled, fetch a course and cache it. Update the same course using **native SQL** (bypassing Hibernate), so the L2 cache is never invalidated. Fetch the course again and observe the stale value. Then manually evict the entity from L2 cache and fetch again to see the fresh value.

### Success criteria

- Session 1 caches course X in L2
- Native SQL updates the course name in the database directly
- Session 2 fetches and sees the OLD name (stale from L2 cache)
- Session 3, after manual eviction, fetches and sees the NEW name
- Conclusion: L2 cache correctness depends on all writes flowing through Hibernate

### Hints

<details>
<summary>Hint 1</summary>

Native SQL in Hibernate:

```java
try (Session session = sessionFactory.openSession()) {
    Transaction tx = session.beginTransaction();
    session.createNativeQuery(
        "update courses set name = ? where id = ?")
        .setParameter(1, "Updated via SQL")
        .setParameter(2, courseId)
        .executeUpdate();
    tx.commit();
}
```

Or use the `DemoData.updateStudentEmailViaJdbc()` pattern if available in your starter.

</details>

<details>
<summary>Hint 2</summary>

After native SQL, before manual eviction:

```java
try (Session session = sessionFactory.openSession()) {
    Course stale = session.find(Course.class, courseId);
    System.out.println("Stale value: " + stale.getName()); // Old name still in cache
}
```

</details>

<details>
<summary>Hint 3</summary>

Manual eviction:

```java
sessionFactory.getCache().evictEntityData(Course.class, courseId);
```

After this, the L2 cache no longer holds the course entry.

</details>

<details>
<summary>Hint 4</summary>

After eviction:

```java
try (Session session = sessionFactory.openSession()) {
    Course fresh = session.find(Course.class, courseId);
    System.out.println("Fresh value: " + fresh.getName()); // New name from DB
}
```

Expected statistics:

```
Before eviction: L2 hit (stale cached value)
After eviction: L2 miss + 1 query (fresh fetch from DB)
```

</details>

---

# Bonus Challenge (Optional)

Implement a `CacheWarmer` class that preloads all high-use courses into L2 cache at application startup:

```java
public static void warmCache(SessionFactory sessionFactory) {
    try (Session session = sessionFactory.openSession()) {
        session.createQuery("from Course", Course.class).getResultList();
    }
}
```

Measure the cold-start time (initial fetch) vs. warm-start time (after warmup). Discuss the trade-off: startup delay now vs. faster initial production traffic.

---

# Reflection Questions

### Implementation & Trade-offs

1. You chose `CacheConcurrencyStrategy.READ_WRITE` for Course entities. What would happen if you chose `READ_ONLY` instead? When might read-only be safer?

2. L2 cache adds memory overhead and invalidation complexity. Under what conditions would you choose **not** to use L2 cache, despite the database load?

### Production Readiness

3. In a distributed system with multiple app servers, each with its own L2 cache, how can servers stay coherent? What are the implications for native SQL updates?

4. What monitoring and alerting would you add to detect stale data in production? (Hint: compare database reads with cache statistics.)

### Debugging & Problem Solving

5. A user reports seeing old data after an update. Your logs show the ORM update succeeded, but the user got stale data. Explain two possible causes and how to diagnose each.

6. You observe high L2 cache miss rates (> 50%) despite supposedly caching everything. What could be causing this? How would you investigate?

### Adaptation / Transfer

7. How would L2 cache strategy differ for a read-heavy analytics dashboard vs. a real-time transactional system? Propose different invalidation ranges for each.

8. If Hibernate supported a "time-to-live" (TTL) for L2 cache entries, how would you configure it for student records vs. course records in your university domain?

---
