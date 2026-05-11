# Day 5 Demos

This project contains one runnable demo per major section of the ORM caching and performance morning.

## Demos

- `Demo1WhyCachingMatters`: repeated reads, duplicate objects, and a tiny local cache
- `Demo2FirstLevelCache`: Hibernate session cache, identity guarantee, and cache lifetime
- `Demo3SecondLevelCache`: L2 cache hits across sessions, invalidation, and native SQL staleness risk
- `Demo4CachingPitfalls`: multiple bag fetches, row explosion risk, and overfetching symptoms
- `Demo5OptimizationChoices`: compare naive entity loading, join fetch for details, and projections for list screens
- `Demo6WrapUpDecisions`: quick decision checklist for cache scope, invalidation, and measurements

## Run

Compile everything:

```bash
mvn compile
```

Run one demo:

```bash
mvn -q -Dexec.mainClass=com.example.demos.Demo1WhyCachingMatters exec:java
mvn -q -Dexec.mainClass=com.example.demos.Demo2FirstLevelCache exec:java
mvn -q -Dexec.mainClass=com.example.demos.Demo3SecondLevelCache exec:java
mvn -q -Dexec.mainClass=com.example.demos.Demo4CachingPitfalls exec:java
mvn -q -Dexec.mainClass=com.example.demos.Demo5OptimizationChoices exec:java
mvn -q -Dexec.mainClass=com.example.demos.Demo6WrapUpDecisions exec:java
```
