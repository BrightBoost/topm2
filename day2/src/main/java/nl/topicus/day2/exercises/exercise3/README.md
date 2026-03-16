# Exercise 3: Isolation Levels — Which Level Fits the Scenario?

This is a **theory exercise** — no code to write.
The answers below are the discussion solutions.

---

## Part 1: Dashboard with "live" statistics

**Scenario**: Internal dashboard showing total active students every 10 seconds.
Approximate count is fine. Performance is critical over millions of rows.

**Answer: READ_UNCOMMITTED**

- Dirty reads are acceptable — the number is a rough indication, not a financial report.
- Non-repeatable reads and phantom reads don't matter either.
- READ_UNCOMMITTED is the fastest because no isolation overhead is needed.
- A stricter level (READ_COMMITTED) would cost more resources for no real benefit.

---

## Part 2: Student enrollment overview

**Scenario**: Student opens profile page. Two queries in one transaction: list of courses + total credits.
Both queries must be consistent with each other.

**Answer: REPEATABLE_READ**

- Between the two reads, another transaction could update credits (non-repeatable read).
- At READ_COMMITTED, the second query could see different data → inconsistent page.
- REPEATABLE_READ guarantees data read once stays stable for the transaction duration.
- Phantom reads (new rows appearing) are acceptable — they show up on next page load.
- SERIALIZABLE would be overkill — it prevents phantoms that are not a problem here.

---

## Part 3: Salary calculation

**Scenario**: End-of-month batch job calculating teacher salaries. Reads base salary,
bonuses, deductions. No data may change or appear during the calculation.
Runs at night — performance is secondary.

**Answer: SERIALIZABLE**

- Must prevent ALL concurrency problems: dirty reads, non-repeatable reads, AND phantom reads.
- New bonus records appearing mid-calculation (phantoms) would cause incorrect salaries.
- Only SERIALIZABLE prevents all three.
- Performance cost (more locks, potential deadlocks) is irrelevant — runs at night with no concurrent users.
- REPEATABLE_READ is insufficient — it still allows phantom reads.

---

## Part 4: Course catalog

**Scenario**: Hundreds of students browsing the catalog simultaneously. Must not see
uncommitted changes. Stale data by a few seconds is fine. Read-heavy, performance critical.

**Answer: READ_COMMITTED**

- Must prevent dirty reads — students should not see course changes that might be rolled back.
- Non-repeatable reads: acceptable (data may change between page loads).
- Phantom reads: acceptable (new courses appear on reload).
- READ_COMMITTED is the standard for PostgreSQL, Oracle, and Topicus — best balance of correctness and performance.
- READ_UNCOMMITTED would risk showing rolled-back data.
- REPEATABLE_READ would add unnecessary overhead for a read-heavy catalog.

---

## Summary Table

| Scenario           | Level            | Key Reasoning                       |
| ------------------ | ---------------- | ----------------------------------- |
| Live dashboard     | READ_UNCOMMITTED | Approximate OK, max performance     |
| Student profile    | REPEATABLE_READ  | Consistent reads within transaction |
| Salary calculation | SERIALIZABLE     | Zero tolerance for any anomaly      |
| Course catalog     | READ_COMMITTED   | No dirty reads, high concurrency    |
