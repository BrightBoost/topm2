# Mini Exercise 3: ORM Performance Optimization — Choosing the Right Strategy

## Scenario / Context

Your university application has attracted real traffic. The new student roster view loads a list of 100 students, and the backend takes 8 seconds to respond. There's clearly a performance problem. Your team runs the SQL logs and finds hundreds of queries for a simple list view: one to fetch students, then N queries to load enrollments per student (the N+1 problem). The team debates: should we add caching? Join fetch everything? Use projections? After this exercise, you'll understand that caching is one tool among many. The real wins come from query design which helps knowing which fetching strategy fits each use case (list screens, detail screens, graphs). You'll write three versions of the same feature, measure them, and make informed trade-offs.

---

## Learning Goals

- Reproduce the N+1 problem in a realistic list-screen scenario
- Recognize when projections are the right tool: for read-only data, minimal columns, single query
- Apply JOIN FETCH to full entity graphs when detail screens need lazy relationships resolved
- Compare naive, projection-based, and JOIN FETCH approaches using statistics (query count, fetch count)
- Understand that caching amplifies good query design but cannot fix poor fetching strategies
- Build a decision framework: profile first, measure results, choose deliberately
- Evaluate the memory, latency, and complexity trade-offs of each optimization

---

# Lab Parts

This lab contains **4 parts**.

---

## Part 1: Reproduce the N+1 Problem (Naive List Loading)

### What you will do

Seed test data: 5 students, each with 2 enrollments. Write code that loads all students and then accesses each student's department name and enrollment count. Measure SQL queries and collection fetches. Observe that naive listing produces 1 + N + N\*M queries unnecessarily.

### Success criteria

- Query 1: `SELECT s FROM Student s` (loads 5 students)
- Query 2+: For each student, `SELECT ...enrollments...` (5 queries for lazy enrollment collections)
- Statistics show high query count (> 10) for a simple roster
- Console clearly displays the naïve result set and SQL count
- You've identified the performance bottleneck

### Hints

<details>
<summary>Hint 1</summary>

Start in the `testNaiveListScreen()` method:

```java
try (Session session = sessionFactory.openSession()) {
    sessionFactory.getStatistics().clear();

    List<Student> students = session.createQuery(
        "SELECT s FROM Student s", Student.class
    ).getResultList();

    for (Student s : students) {
        System.out.println(s.getName() + " | " +
            s.getDepartment().getName() + " | " +
            s.getEnrollments().size());
    }

    printStatistics(sessionFactory, "Naive list");
}
```

</details>

<details>
<summary>Hint 2</summary>

The `printStatistics()` helper shows query count, collection fetch count, etc. Look for:

```
Queries executed: 11
Collections fetched: 5
```

This means 1 query for students, 5 queries for enrollments = 6. Plus accessing department might add more.

</details>

<details>
<summary>Hint 3</summary>

If you don't see 10+ queries, verify that `hibernate.show_sql=true` and `show_sql` is enabled in your logs. You should see a SELECT for students and then a SELECT per enrollment.

</details>

<details>
<summary>Hint 4</summary>

Expected output pattern:

```
=== Naive list (5 students, 2 enrollments each) ===
Student 1 | Computer Science | 2 enrollments
Student 2 | Computer Science | 2 enrollments
...
[Statistics] Naive list:
  Queries executed: 11
  Collections fetched: 5
```

The goal is to see the problem clearly before fixing it.

</details>

---

## Part 2: Optimize with Projections (List Screen)

### What you will do

Rewrite the query using a **constructor projection** that fetches only the three fields the UI needs: `name`, `email`, and `departmentName`. Use a JPQL constructor that maps directly to your `StudentListRow` DTO. Observe that one query replaces the N+1 queries.

### Success criteria

- Single JPQL query using `SELECT new com.example.dto.StudentListRow(s.name, s.email, d.name)`
- One SELECT statement in the database
- Statistics show 1 query vs. 11 in the naive approach
- `StudentListRow` records match the three-column projection
- Result set is rendered the same to the user (same information, less data moved)

### Hints

<details>
<summary>Hint 1</summary>

Define the DTO (or use the provided `StudentListRow` record):

```java
public record StudentListRow(String name, String email, String departmentName) {
}
```

The constructor must match the projection order.

</details>

<details>
<summary>Hint 2</summary>

Write the projection query:

```java
try (Session session = sessionFactory.openSession()) {
    sessionFactory.getStatistics().clear();

    List<StudentListRow> rows = session.createQuery(
        "SELECT new com.example.dto.StudentListRow(s.name, s.email, d.name) " +
        "FROM Student s JOIN s.department d",
        StudentListRow.class
    ).getResultList();

    for (StudentListRow row : rows) {
        System.out.println(row.name() + " | " +
            row.email() + " | " + row.departmentName());
    }

    printStatistics(sessionFactory, "Projection list");
}
```

</details>

<details>
<summary>Hint 3</summary>

Expected statistics:

```
[Statistics] Projection list:
  Queries executed: 1
  Collections fetched: 0
```

Note: collections fetched = 0 because we're not loading collections at all.

</details>

<details>
<summary>Hint 4</summary>

Projection advantage: minimal data transferred, one query, read-only. Disadvantage: returns a DTO, not living entities (no lazy loading, no dirty checking).

Use projections for:

- List screens (read-only)
- Reports
- APIs that just need summaries

Don't use for:

- Detail screens that need full entity graphs
- Data you'll modify

</details>

---

## Part 3: Optimize with JOIN FETCH (Detail Screen)

### What you will do

Rewrite the query as a detail-screen scenario: fetch one student and their full graph (department, enrollments, courses). Use `JOIN FETCH` to eagerly load the relationships in a single query, then access all data without triggering additional queries.

### Success criteria

- JPQL query with `LEFT JOIN FETCH s.department` and `LEFT JOIN FETCH s.enrollments e LEFT JOIN FETCH e.course`
- Single or minimal queries (depends on join cardinality; use `DISTINCT` for deduplication)
- All relationships are resolved without additional lazy-loading queries
- Statistics show roughly the same or fewer queries than naive approach but in one pull
- You can access `student.getDepartment()`, `s.getEnrollments()`, `e.getCourse()` without extra queries

### Hints

<details>
<summary>Hint 1</summary>

Use JOIN FETCH and DISTINCT:

```java
try (Session session = sessionFactory.openSession()) {
    sessionFactory.getStatistics().clear();

    Student student = session.createQuery(
        "SELECT DISTINCT s FROM Student s " +
        "LEFT JOIN FETCH s.department " +
        "LEFT JOIN FETCH s.enrollments e " +
        "LEFT JOIN FETCH e.course " +
        "WHERE s.id = :id",
        Student.class
    )
    .setParameter("id", 1L)
    .getSingleResult();

    System.out.println("Student: " + student.getName());
    System.out.println("Department: " + student.getDepartment().getName());
    for (Enrollment enr : student.getEnrollments()) {
        System.out.println("  Course: " + enr.getCourse().getName());
    }

    printStatistics(sessionFactory, "JOIN FETCH detail");
}
```

</details>

<details>
<summary>Hint 2</summary>

Why `LEFT JOIN FETCH` instead of `JOIN FETCH`?

- `LEFT JOIN FETCH` includes students even if they have no enrollments
- `JOIN FETCH` would exclude students without enrollments

Choose based on your business logic.

</details>

<details>
<summary>Hint 3</summary>

DISTINCT is needed because the SQL result might have duplicate rows if a student has multiple enrollments (Cartesian product row multiplication). Hibernate's `SELECT DISTINCT` removes duplicates in the result set before building entities.

</details>

<details>
<summary>Hint 4</summary>

Expected statistics for detail screen:

```
[Statistics] JOIN FETCH detail:
  Queries executed: 1
  Collections fetched: 1
```

One query loads all data eagerly. No lazy loading surprises in the application.

</details>

---

## Part 4: Decision Framework and Comparison

### What you will do

Compare the three approaches in a structured way (as a bonus you can add the L2 cache + naive approach). For each scenario (list screen, detail screen, report), recommend the best strategy. Create a summary table with metrics: queries, latency, memory, code complexity.

### Success criteria

- Written or printed comparison table with: approach, # queries, suitable for list/detail/report
- Clear recommendations: use projections for lists, JOIN FETCH for detail, caching for read-heavy scenarios
- Explanation of the memory and latency trade-offs
- Decision checklist: "Does the screen need all relationships? Is it read-only? How many rows?"
- Acknowledgment that no single strategy is best for all cases

### Hints

<details>
<summary>Hint 1</summary>

Build a comparison table:

```
Approach | Queries | Latency | Memory | Best For
---------|---------|---------|--------|----------
Naive    | N+M     | High    | Medium | Educational example (avoid!)
Projection | 1     | Low     | Low    | List screens, read-only
JOIN FETCH | 1-2   | Medium  | High   | Detail screens, full graphs
L2 Cache + Naive | 1 then cached | Low | High | Repeated reads of same entity
```

</details>

<details>
<summary>Hint 2</summary>

Decision checklist:

```
1. Is the view read-only (no updates)?
   -> Use projections

2. Do you need a full entity graph and might update later?
   -> Use JOIN FETCH + careful lazy loading

3. Is the same data read repeatedly across requests?
   -> Consider L2 cache

4. Is it a high-traffic endpoint with many concurrent requests?
   -> Profile first; optimize queries before adding cache
```

</details>

<details>
<summary>Hint 3</summary>

Real-world note: many applications forget to measure. They assume caching is the answer and add complexity. In practice:

- Query optimization often yields 10-100x improvement
- Caching yields 2-5x improvement (but adds staleness risk)
- Always measure before and after

</details>

<details>
<summary>Hint 4</summary>

Print your recommendations to console. Example output:

```
=== ORM OPTIMIZATION DECISION FRAMEWORK ===

Use PROJECTIONS for:
- List screens (read-only)
- Minimal columns needed
- High-volume, read-heavy endpoints
Example: course catalog list

Use JOIN FETCH for:
- Detail screens (full entity graph)
- Relationships needed together
- Lower cardinality (not thousands of rows)
Example: course detail page with prerequisites, enrollments

Use L2 CACHE for:
- Stable reference data (departments, courses)
- Repeated cross-request reads
- Accept staleness window of hours/days

MEASURE FIRST. DO NOT GUESS.
```

</details>

---

# Bonus Challenge (Optional)

Implement a **performance benchmark** that runs all three approaches 10 times each and reports average query count, average time (rough), and memory allocated. Use `System.nanoTime()` and `Runtime.getRuntime().totalMemory()` for measurements.

Example output:

```
Approach       | Avg Queries | Avg Time (ms) | Total Runs
---------------|-------------|---------------|-----------
Naive          | 11.0        | 45.2          | 10
Projection     | 1.0         | 8.1           | 10
JOIN FETCH     | 1.2         | 12.3          | 10

Recommendation: Use projection for list screens (8x faster, 91% fewer queries).
```

---

# Reflection Questions

### Implementation & Trade-offs

1. A projection returns a DTO, not a Hibernate entity. What are the consequences of losing dirty checking, lifecycle callbacks, and lazy loading in a DTO?

2. You used `SELECT DISTINCT` in JOIN FETCH to avoid row multiplication. When would DISTINCT hide a real data problem?

### Production Readiness

3. A detail screen using JOIN FETCH might trigger a "row explosion" if there are multiple collection joins. How do you detect and prevent this? (Hint: `MultipleBagFetchException`.)

4. What monitoring would you add to alert when query counts spike unexpectedly? How often should you re-profile your hottest endpoints?

### Debugging & Problem Solving

5. Your projection query returns incorrect totals or missing rows. Walk through the debugging process: how would you verify the SQL is correct, and that the DTO constructor matches?

6. A developer added a new lazy-loaded relationship to Student. The list screen breaks, loading suddenly slow. How would you catch this in code review or testing?

### Adaptation / Transfer

7. How would your optimization strategy differ for a bulk data export (1 million students) vs. a web request (5 students)? What new concerns arise?

8. If you moved from a relational database (PostgreSQL) to a document store (MongoDB), would projections still apply? Would JOIN FETCH be different?

---
