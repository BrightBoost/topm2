# Mini Exercise 1: First-Level Cache — Identity and Reuse Within a Session

## Scenario / Context

You load a course record in your application. Later, the same course is fetched again through a different query path. Without caching, each fetch would be a database round-trip. But Hibernate has a secret weapon: the first-level cache. It lives inside every `Session` and automatically keeps track of all loaded entities. The same database row across multiple lookup paths returns the same Java object instance without additional db queries. In this exercise, you'll watch the first-level cache in action, observe identity guarantees, and understand why sessions are the boundaries of cache scope.

---

## Learning Goals

- Recognize the first-level cache as the automatic per-session persistence context
- Reproduce cache hits and misses by inspecting SQL logged to the console
- Prove the identity guarantee: multiple lookups of the same ID return the same Java instance
- Show how different query paths (by ID, by column value, JPQL) reuse the same cached entity
- Understand that the first-level cache is session-scoped and disappears when the session closes
- Evaluate when detaching entities (closing the session) affects change tracking and object identity

---

# Lab Parts

This lab contains **4 parts**.

---

## Part 1: Observe L1 Cache Hits and Misses

### What you will do

Open a single session and fetch the same course twice by ID using `session.find()`. The first call should generate a SELECT statement. The second call should **not** generate any SQL because Hibernate returns the cached instance. Enable printing the SQL logs to prove it.

### Success criteria

- First `find()` generates one SELECT statement
- Second `find()` by the same ID generates no SQL
- Both references are identical Java objects (confirmed with `==` operator)
- Console output clearly shows the SQL generation and cache behavior

### Hints

<details>
<summary>Hint 1</summary>

Run the starter `ExerciseL1Cache.java` and implement Part 1 in the `// TODO: Implement part 1` section.

</details>

<details>
<summary>Hint 2</summary>

To get the first course ID, query the database in a separate session before running the cache test:

```java
try (Session session = sessionFactory.openSession()) {
    Long courseId = session.createQuery(
        "select c.id from Course c order by c.id", Long.class)
        .setMaxResults(1)
        .getSingleResult();
    // use this ID in Part 1
}
```

</details>

<details>
<summary>Hint 3</summary>

Within one session, fetch the same course twice:

```java
try (Session session = sessionFactory.openSession()) {
    System.out.println("First lookup by ID:");
    Course first = session.find(Course.class, courseId);

    System.out.println("\nSecond lookup by ID (expect no SQL):");
    Course second = session.find(Course.class, courseId);

    System.out.println("Same instance? " + (first == second));
}
```

</details>

<details>
<summary>Hint 4</summary>

The console should show something like:

```
First lookup by ID:
select c1_0.id, c1_0.credits, c1_0.name from courses c1_0 where c1_0.id = ?

Second lookup by ID (expect no SQL):
Same instance? true
```

Note the absence of a second SELECT. That's the L1 cache in action.

</details>

---

## Part 2: Identity Guarantee Across Query Paths

### What you will do

In the same session, fetch a course by ID and also fetch it by name using a JPQL query. Even though the paths are different, both should return the same Java instance. This is the identity guarantee: within a session, one database row always maps to exactly one Java object.

### Success criteria

- Course is fetched by ID using `session.find()`
- Same course is fetched by name using `session.createQuery()`
- Both return the same Java instance (`first == second` is true)
- SQL shows only two queries: the initial SELECT by ID and the SELECT by name
- The second SELECT uses the name condition but does **not** create a new Course object

### Hints

<details>
<summary>Hint 1</summary>

After fetching a course by ID in Part 1, save its name to a variable. Then query the database by that name.

</details>

<details>
<summary>Hint 2</summary>

```java
// In the same session...
Course byId = session.find(Course.class, courseId);
String name = byId.getName();

Course byName = session.createQuery(
    "select c from Course c where c.name = :name", Course.class)
    .setParameter("name", name)
    .getSingleResult();

System.out.println("Same instance? " + (byId == byName));
```

</details>

<details>
<summary>Hint 3</summary>

The SQL logs should show two SELECT statements, but notice: the first SELECT fetches the course by ID and populates the cache. The second SELECT by name is issued because the query executor needs to resolve the `WHERE` clause, but the result set is then matched against the cache. Hibernate recognizes it's the same row and returns the cached instance instead of creating a new one.

</details>

<details>
<summary>Hint 4</summary>

If you see `(first == second)` print as `false`, doublecheck that both calls were within the same session. Opening a new session would break the identity guarantee.

</details>

---

## Part 3: New Session, Cache Boundary

### What you will do

Close the first session. Open a new session and fetch the same course by ID again. Observe that SQL is generated **again**: the first-level cache is per session, not global.

### Success criteria

- First session fetches the course (generates one SELECT statement)
- First session is closed
- New session fetches the same course (generates a new SELECT statement)
- The two instances are **not** the same Java object (== comparison is false)
- Console clearly shows the session lifecycle and cache boundaries

### Hints

<details>
<summary>Hint 1</summary>

Use try-with-resources for automatic session closing:

```java
try (Session session1 = sessionFactory.openSession()) {
    Course first = session1.find(Course.class, courseId);
    System.out.println("First session: " + first);
}
// session1 is automatically closed here; cache is gone

try (Session session2 = sessionFactory.openSession()) {
    System.out.println("Opening new session...");
    Course second = session2.find(Course.class, courseId);
    System.out.println("Second session: " + second);
    System.out.println("Same instance? false (different sessions)");
}
```

</details>

<details>
<summary>Hint 2</summary>

Even though the JavaScript object `first` and `second` might have the same field values, their Java identity (`==`) will be false because each session maintains its own cache.

</details>

<details>
<summary>Hint 3</summary>

Expected output pattern:

```
=== PART 3: New Session, Cache Reset ===

First session:
select c1_0.id, c1_0.credits, c1_0.name from courses c1_0 where c1_0.id = ?
Course{id=1, name='Java Fundamentals', credits=5}

Opening new session...
select c1_0.id, c1_0.credits, c1_0.name from courses c1_0 where c1_0.id = ?
Second session:
Course{id=1, name='Java Fundamentals', credits=5}

Instances the same? false
```

</details>

---

## Part 4: Detached Entities and Session Scope

### What you will do

Fetch a course in one session (it's persistent/managed). Close the session (course becomes detached). Modify the course object outside any session. Open a new session and persist the modified course. Observe what happens.

### Success criteria

- Course is fetched in a session and its name is cached
- Session is closed; course is detached
- Course object is modified (e.g., `course.setName("Updated Name")`)
- New session is opened and course is merged/persisted
- Changes are flushed to the database (or show dirty checking behavior if you use `find` instead of `merge`)

### Hints

<details>
<summary>Hint 1</summary>

Detached entities are still Java objects, but they are no longer tracked by any session. Changes to them are not automatically persisted.

</details>

<details>
<summary>Hint 2</summary>

To reattach a detached entity, use `session.merge()`:

```java
try (Session session1 = sessionFactory.openSession()) {
    Transaction tx = session1.beginTransaction();
    Course course = session1.find(Course.class, courseId);
    String originalName = course.getName();
    tx.commit();
} // session1 closed; course is detached

System.out.println("Modifying detached course...");
course.setName(originalName + " [Updated]");

try (Session session2 = sessionFactory.openSession()) {
    Transaction tx = session2.beginTransaction();
    Course merged = session2.merge(course);
    tx.commit();
}

// Verify in a new session that the change persisted
try (Session session3 = sessionFactory.openSession()) {
    Course reloaded = session3.find(Course.class, courseId);
    System.out.println("After merge: " + reloaded.getName());
}
```

</details>

<details>
<summary>Hint 3</summary>

If you persist the detached entity again without merging, Hibernate might throw an exception if the ID is already assigned. `merge()` is the right tool for reattaching detached entities with updates.

</details>

<details>
<summary>Hint 4</summary>

Expected sequence:

```
Modifying detached course...
(no SQL yet — changes are local to the Java object)

Opening new session and merging...
select c1_0.id, c1_0.credits, c1_0.name from courses c1_0 where c1_0.id = ?
update courses set credits = ?, name = ? where id = ?

After merge:
select c1_0.id, c1_0.credits, c1_0.name from courses c1_0 where c1_0.id = ?
After merge: Java Fundamentals [Updated]
```

</details>

---

# Bonus Challenge (Optional)

Implement a helper method `findCourseInSession(sessionFactory, courseId, int sessionCount)` that opens `sessionCount` separate sessions and fetches the same course. Print a statistics summary:

```
Session 1: 1 SELECT query
Session 2: 1 SELECT query
Session 3: 1 SELECT query
Total queries: 3

Conclusion: Each session has its own L1 cache; no cross-session reuse.
```

Then explain how a shared second-level cache could reduce this. (You don't need to implement L2 yet, just describe what it would do.)

---

# Reflection Questions

### Implementation & Trade-offs

1. Why does Hibernate guarantee that `session.find(id) == session.find(id)` returns the same Java object? What would break if identity were not guaranteed?

2. You modified a course name outside a session. What are the memory and complexity implications of tracking detached entities so they can be re-attached?

### Production Readiness

3. In a web application where each HTTP request gets its own session, what does the session-scoped L1 cache mean for memory usage and request latency? How does this scale to 1000 simultaneous requests?

4. If you accidentally close a session while holding references to detached entities and then try to access lazy-loaded fields, what exception would you expect? How would you prevent it?

### Debugging & Problem Solving

5. You have a page that loads student records in a loop, and logs show 100 SQL queries for 1 loop iteration of 100 students. What is the likely cause, and how does understanding L1 cache help you diagnose it?

6. Why might `session.find()` and a JPQL query return different results if run consecutively, even though they query the same entity? (Hint: think about query result projection vs. entity materialization.)

### Adaptation / Transfer

7. How would the session-scoped cache behavior differ in a batch job that loads 1 million records? Propose a strategy to avoid memory bloat while still benefiting from L1 cache.

8. In a multi-threaded application, can two threads share the same Hibernate `Session`? Explain your reasoning using what you learned about the L1 cache and object identity.

---
