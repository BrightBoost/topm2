# Mini Exercise 3:  Het N+1 probleem opsporen en oplossen

## Scenario / Context

Je applicatie draait, de entiteiten zijn gemapt, alles werkt. Maar je collega kijkt naar de logs en zegt: "Waarom vuurt Hibernate 51 queries af voor een simpel overzicht van 50 studenten?" Welkom bij het N+1 probleem — het meest beruchte performanceprobleem in de ORM-wereld. Je gaat het probleem eerst reproduceren en met eigen ogen tellen hoeveel queries Hibernate genereert. Daarna probeer je twee oplossingen: `JOIN FETCH` (chirurgisch precies) en `@BatchSize` (breed vangnet). Tot slot ga je experimenteren met fetch-types en maak je kennis met de `LazyInitializationException`.

---

## Learning Goals

- Het N+1 probleem reproduceren door SQL-queries te tellen in de console-output
- Een `JOIN FETCH`-query schrijven in JPQL om gerelateerde data in één query op te halen
- `@BatchSize` configureren op een collectie en het effect op het aantal queries verifiëren
- Het verschil tussen `FetchType.LAZY` en `FetchType.EAGER` observeren en beredeneren wanneer je welke kiest
- Een `LazyInitializationException` opwekken en strategieën formuleren om deze te voorkomen
- De gegenereerde SQL analyseren als diagnostisch middel voor performanceproblemen

---

## Prerequisites

- Java 21 geïnstalleerd
- Maven geïnstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Mini-exercise 2 afgerond (werkende entiteiten `Student`, `Course`, `Enrollment` met relaties)
- `hibernate.show_sql=true` ingeschakeld in `HibernateUtil`
- Basiskennis van JPQL (dag 3: queries schrijven met `session.createQuery()`)

---

# Lab Parts

Dit lab bevat **4 delen**.

---

## Part 1: Het N+1 probleem reproduceren

### What you will do

Maak testdata aan: minimaal 5 studenten, elk ingeschreven voor 2-3 cursussen. Schrijf daarna code die alle studenten ophaalt en voor elke student de enrollments uitprint. Tel het aantal SQL-queries dat Hibernate genereert in de console. Dit is het N+1 probleem in actie.

### Success criteria

- Er zijn minimaal 5 studenten in de database, elk met 2-3 enrollments
- De code haalt alle studenten op met `SELECT s FROM Student s`
- De code itereert over elke student en benadert `student.getEnrollments()`
- In de console zijn meer queries zichtbaar dan strikt noodzakelijk (1 voor studenten + N voor enrollments)
- Je hebt het exacte aantal queries geteld en opgeschreven

### Hints

<details>
<summary>Hint 1</summary>

Het N+1 probleem ontstaat omdat `@OneToMany` standaard `FetchType.LAZY` is. Hibernate haalt de enrollments pas op als je ze daadwerkelijk benadert. Per student is dat een aparte query.

</details>

<details>
<summary>Hint 2</summary>

Tel de queries door in de console te zoeken naar `select`. Elke `select`-regel is een database-query. Met 5 studenten verwacht je 1 (studenten ophalen) + 5 (enrollments per student) = 6 queries.

</details>

<details>
<summary>Hint 3</summary>

Maak een aparte methode voor het seeden van data, zodat je het makkelijk opnieuw kunt draaien. Gebruik de `student.enroll(course)` helper uit de bonus van exercise 2, of zet beide kanten handmatig.

</details>

<details>
<summary>Hint 4</summary>

```java
// Data seeden (in een aparte methode/transactie)
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    var tx = session.beginTransaction();
    Course c1 = new Course("Databases", 3, "SQL");
    Course c2 = new Course("Web Dev", 4, "HTML/CSS/JS");
    Course c3 = new Course("Security", 3, "Cybersecurity");
    session.persist(c1); session.persist(c2); session.persist(c3);

    for (int i = 1; i <= 5; i++) {
        Student s = new Student("Student " + i, "S" + i, "s" + i + "@uni.nl");
        // Maak enrollments aan en zet beide kanten...
        session.persist(s);
    }
    tx.commit();
}

// N+1 probleem triggeren
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    List<Student> students = session.createQuery(
        "SELECT s FROM Student s", Student.class).getResultList();

    System.out.println("--- Nu worden enrollments lazy geladen ---");
    for (Student s : students) {
        System.out.println(s.getName() + " heeft "
            + s.getEnrollments().size() + " enrollments");
    }
}
// Tel de select-statements in de output!
```

</details>

---

## Part 2: Oplossing 1 — JOIN FETCH

### What you will do

Schrijf een JPQL-query met `JOIN FETCH` die studenten en hun enrollments in één query ophaalt. Voer dezelfde iteratie uit als in Part 1 en tel opnieuw het aantal queries. Vergelijk de gegenereerde SQL met de SQL uit Part 1.

### Success criteria

- De JPQL-query bevat `JOIN FETCH s.enrollments`
- Alle studenten met hun enrollments worden opgehaald in **één** SQL-query
- De gegenereerde SQL toont een `JOIN` (geen apart `SELECT` per student)
- Het totale aantal queries is gedaald van N+1 naar 1

### Hints

<details>
<summary>Hint 1</summary>

`JOIN FETCH` is geen gewone `JOIN`. Een gewone `JOIN` filtert resultaten, maar haalt de gerelateerde objecten niet op. `JOIN FETCH` zegt: "Haal de gerelateerde objecten op en vul de collectie meteen."

</details>

<details>
<summary>Hint 2</summary>

Vervang de query `SELECT s FROM Student s` door `SELECT s FROM Student s JOIN FETCH s.enrollments`. De rest van je code (de loop) kan precies hetzelfde blijven.

</details>

<details>
<summary>Hint 3</summary>

Let op: `JOIN FETCH` kan duplicaten opleveren als een student meerdere enrollments heeft. Gebruik `SELECT DISTINCT s FROM Student s JOIN FETCH s.enrollments` of `stream().distinct()` als je duplicaten ziet.

</details>

<details>
<summary>Hint 4</summary>

```java
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    List<Student> students = session.createQuery(
        "SELECT DISTINCT s FROM Student s JOIN FETCH s.enrollments",
        Student.class).getResultList();

    System.out.println("--- Met JOIN FETCH ---");
    for (Student s : students) {
        System.out.println(s.getName() + " heeft "
            + s.getEnrollments().size() + " enrollments");
    }
}
// Nu zou je slechts 1 select-statement moeten zien!
```

</details>

---

## Part 3: Oplossing 2 — @BatchSize

### What you will do

Verwijder de `JOIN FETCH`-query en ga terug naar de simpele `SELECT s FROM Student s`. Voeg `@BatchSize(size = 5)` toe aan de `enrollments`-collectie in `Student`. Draai dezelfde code en tel opnieuw de queries. Bekijk de SQL en zoek naar het `IN`-clause patroon.

### Success criteria

- De `@BatchSize(size = 5)` annotatie staat op `Student.enrollments`
- De oorspronkelijke query zonder `JOIN FETCH` wordt gebruikt
- Het aantal queries is gedaald: 1 voor studenten + 1 (of enkele) voor enrollments (in batches)
- De gegenereerde SQL toont een `WHERE student_id IN (?, ?, ?, ?, ?)` patroon
- Je kunt het verschil uitleggen tussen `JOIN FETCH` (per query) en `@BatchSize` (op de entity)

### Hints

<details>
<summary>Hint 1</summary>

`@BatchSize` verandert niets aan je queries — het verandert hoe Hibernate lazy loading uitvoert. In plaats van één query per student, laadt Hibernate de enrollments van meerdere studenten tegelijk in één query met een `IN`-clause.

</details>

<details>
<summary>Hint 2</summary>

Voeg de annotatie toe boven de collectie in `Student`:

```java
@OneToMany(mappedBy = "student", cascade = CascadeType.PERSIST)
@BatchSize(size = 5)
private List<Enrollment> enrollments = new ArrayList<>();
```

Importeer `org.hibernate.annotations.BatchSize`.

</details>

<details>
<summary>Hint 3</summary>

Met 5 studenten en `@BatchSize(size = 5)` verwacht je 2 queries totaal: 1 voor de studenten, 1 voor alle enrollments (gebatcht). Met 12 studenten en batchgrootte 5 zou je 1 + 3 queries krijgen (batches van 5, 5 en 2).

</details>

<details>
<summary>Hint 4</summary>

Vergelijk de twee oplossingen:

|                          | JOIN FETCH                             | @BatchSize                            |
| ------------------------ | -------------------------------------- | ------------------------------------- |
| Waar configureer je het? | In de JPQL query                       | Op de entity-klasse                   |
| Scope                    | Per query                              | Alle queries die deze collectie laden |
| Resultaat                | 1 query (met JOIN)                     | 1 + ceil(N/batchSize) queries         |
| Nadeel                   | Moet in elke query die het nodig heeft | Minder controle, altijd actief        |
| Vergelijking             | Chirurgisch mes                        | Breed vangnet                         |

</details>

---

## Part 4: Eager vs Lazy experiment

### What you will do

Experimenteer met het fetch-type van de `@ManyToOne` in `Enrollment` (die naar `Course` wijst). Verander het van het standaard `EAGER` naar `LAZY` en observeer het verschil in queries wanneer je een enrollment ophaalt en de coursenaam benadert. Verander het daarna terug en vergelijk.

### Success criteria

- Met `FetchType.EAGER` (standaard): het ophalen van een enrollment haalt de course direct mee in dezelfde query (JOIN)
- Met `FetchType.LAZY`: het ophalen van een enrollment genereert geen query voor course, totdat je `getCourse().getName()` aanroept
- Je kunt uitleggen waarom `@ManyToOne` standaard EAGER is en `@OneToMany` standaard LAZY
- Je kunt een scenario benoemen waarin LAZY beter is voor een `@ManyToOne`

### Hints

<details>
<summary>Hint 1</summary>

De standaard fetch-types volgen een logica: een single-valued associatie (`@ManyToOne`, `@OneToOne`) is standaard EAGER omdat het slechts één extra object is. Een collectie (`@OneToMany`, `@ManyToMany`) is standaard LAZY omdat het potentieel honderden objecten zijn.

</details>

<details>
<summary>Hint 2</summary>

Om het fetch-type te wijzigen:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "course_id")
private Course course;
```

</details>

<details>
<summary>Hint 3</summary>

Met LAZY op `@ManyToOne` krijgt de course een proxy-object. Als je `enrollment.getCourse()` aanroept krijg je de proxy (geen extra query). Pas bij `enrollment.getCourse().getName()` vuurt het proxy een SELECT af.

</details>

<details>
<summary>Hint 4</summary>

```java
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    System.out.println("--- Enrollment ophalen ---");
    Enrollment e = session.get(Enrollment.class, 1L);
    // Met EAGER: course is al geladen (kijk naar de SQL hierboven)
    // Met LAZY: nu komt er een extra SELECT:
    System.out.println("--- Course benaderen ---");
    System.out.println("Course: " + e.getCourse().getName());
}
```

Vergelijk de console-output met EAGER en LAZY.

</details>

---

# Bonus Challenge (Optional)

Provoceer een `LazyInitializationException`. Haal studenten op in een Session, sluit de Session, en probeer dan de enrollments te benaderen. Vang de exception op en implementeer vervolgens een oplossing: haal dezelfde data op met een `JOIN FETCH`-query zodat de collectie al geladen is voordat de Session sluit. Bedenk: in een webapplicatie met Spring, wanneer wordt de Session geopend en gesloten? Hoe voorkom je dit probleem in die context?

```java
// Dit gaat mis:
List<Student> students;
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    students = session.createQuery("SELECT s FROM Student s", Student.class).getResultList();
}
// Session is dicht!
try {
    students.get(0).getEnrollments().size(); // LazyInitializationException!
} catch (LazyInitializationException e) {
    System.out.println("Verwacht: " + e.getMessage());
}
```

---

# Reflectievragen

### Implementatie & Trade-offs

1. `JOIN FETCH` lost het N+1 probleem op, maar je moet het in elke relevante query toevoegen. `@BatchSize` werkt automatisch, maar is minder efficiënt. In welke situatie zou je voor welke oplossing kiezen?
2. De JPA-spec definieert `@ManyToOne` als standaard EAGER en `@OneToMany` als standaard LAZY. Zou je die standaarden aanpassen als je ze zelf mocht kiezen? Waarom wel of niet?

### Production Readiness

3. Hoe zou je in een productieomgeving het N+1 probleem detecteren zonder handmatig queries te tellen? Welke tools of monitoring zou je inzetten?
4. Stel dat je een API bouwt die een lijst van studenten retourneert, soms met enrollments en soms zonder. Hoe zou je de fetch-strategie dynamisch aanpassen op basis van de API-request?

### Debugging & Problem Solving

5. Je ziet 101 queries in de log voor een pagina die 100 producten toont. Welke stappen neem je om te achterhalen welke relatie het N+1 probleem veroorzaakt?
6. Een collega lost de `LazyInitializationException` op door alles `EAGER` te maken. Waarom is dat een slecht idee, en wat stel je als alternatief voor?

### Adaptatie / Transfer

7. Het N+1 probleem bestaat niet alleen in Hibernate. In welke andere technologieën of frameworks (bijv. GraphQL, REST) zou een vergelijkbaar probleem kunnen optreden? Hoe heet het daar?
8. Als je een applicatie bouwt zonder ORM (bijv. met raw JDBC), kun je dan ook een N+1 probleem hebben? Waarom wel of niet?
