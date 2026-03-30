# Mini Exercise: Polymorphism & Proxies

- Create a `Course` superclass with `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` and a `@DiscriminatorColumn`
- Create `OnlineCourse` and `ClassroomCourse` as subtypes, each with their own field (e.g., `platformUrl` for online, `roomNumber` for classroom)
- Persist 2 online courses and 2 classroom courses, retrieve them as `List<Course>`
- Check with `instanceof` whether the correct subtypes are returned
- Provoke the proxy problem: load a `Course` via `session.getReference()` (lazy proxy) and try `instanceof OnlineCourse` — observe the result
- Fix it with `Hibernate.unproxy()` and verify that `instanceof` now works correctly
- **Bonus**: retrieve a `Department` with a lazy `@OneToMany` collection of courses, close the Session, call `getCourses()` — catch the `LazyInitializationException` and think about how you would prevent this

---

# Lab: Polymorfisme, proxies en de grenzen van abstractie

## Scenario / Context

Je teamlead is enthousiast over Hibernate, maar ze heeft een nieuwe eis. De onderwijsinstelling biedt twee soorten cursussen aan: online cursussen (met een platform-URL) en klassikale cursussen (met een lokaalnummer). Ze wil dat je dit modelleert met overerving in Hibernate, zodat beide types in dezelfde tabel worden opgeslagen. "Dat zou toch simpel moeten zijn met Hibernate?" vraagt ze. Nou, deels. Je gaat ontdekken dat overerving in een ORM echt werkt, maar dat er valkuilen zijn die zelfs ervaren developers verrassen. Denk aan proxies die `instanceof`-checks saboteren en lazy loading die exceptions gooit op het moment dat je het het minst verwacht.

---

## Learning Goals

- Single Table inheritance configureren met `@Inheritance`, `@DiscriminatorColumn` en `@DiscriminatorValue`
- Subtypes aanmaken en opslaan, en verifiëren dat Hibernate de juiste typen retourneert bij polymorfe queries
- Het proxy-probleem reproduceren: een lazy-geladen object dat faalt bij een `instanceof`-check
- Het proxy-probleem oplossen met `Hibernate.unproxy()` en beredeneren waarom dit nodig is
- Een `LazyInitializationException` veroorzaken en evalueren welke strategieën dit kunnen voorkomen
- De trade-offs van inheritance mapping in Hibernate beoordelen en vergelijken met het impedance mismatch-probleem

---

## Prerequisites

- Java 21 geïnstalleerd
- Maven geïnstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Mini-exercise 1 en 2 afgerond (entity mapping, CRUD, JPQL)
- Kennis van Java-overerving (`extends`, `instanceof`, casting)
- Basiskennis van het concept "leaky abstractions" uit de slides

---

# Lab Parts

Dit lab bevat **4 delen**.

---

## Part 1: Inheritance mapping opzetten

### What you will do

Pas de `Course`-klasse aan zodat het een superklasse wordt met Single Table inheritance. Voeg een discriminator-kolom toe genaamd `course_type`. Maak twee subklassen: `OnlineCourse` (met een veld `platformUrl`) en `ClassroomCourse` (met een veld `roomNumber`). Registreer alle klassen in `HibernateUtil` en start de applicatie om te verifiëren dat de tabel wordt aangemaakt met de discriminator-kolom en alle velden van beide subtypes.

### Success criteria

- `Course` heeft `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` en `@DiscriminatorColumn(name = "course_type")`
- `OnlineCourse` extends `Course` en heeft `@DiscriminatorValue("ONLINE")` en een `platformUrl`-veld
- `ClassroomCourse` extends `Course` en heeft `@DiscriminatorValue("CLASSROOM")` en een `roomNumber`-veld
- Alle drie de klassen zijn geregistreerd in `HibernateUtil`
- Bij het starten van de applicatie toont de console een `CREATE TABLE courses` met kolommen voor `id`, `name`, `credits`, `course_description`, `course_type`, `platform_url` en `room_number`
- De kolommen `platform_url` en `room_number` staan in dezelfde tabel (dat is Single Table inheritance)

### Hints

<details>
<summary>Hint 1</summary>

Single Table inheritance betekent dat alle subtypes in dezelfde tabel terechtkomen. De discriminator-kolom is een string die aangeeft welk type elke rij is. Hibernate vult deze kolom automatisch in.

</details>

<details>
<summary>Hint 2</summary>

De annotaties op de superklasse zijn: `@Entity`, `@Table`, `@Inheritance(strategy = ...)`, en `@DiscriminatorColumn(name = ...)`. De subklassen krijgen elk `@Entity` en `@DiscriminatorValue("...")`.

</details>

<details>
<summary>Hint 3</summary>

Subklassen hoeven geen `@Table` of `@Id` te hebben — die erven ze van de superklasse. Ze definiëren alleen hun eigen extra velden.

</details>

<details>
<summary>Hint 4</summary>

```java
// Course.java (superklasse)
@Entity
@Table(name = "courses")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "course_type", discriminatorType = DiscriminatorType.STRING)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int credits;
    @Column(name = "course_description")
    private String description;
    // constructors, getters, setters...
}

// OnlineCourse.java
@Entity
@DiscriminatorValue("ONLINE")
public class OnlineCourse extends Course {
    @Column(name = "platform_url")
    private String platformUrl;
    // constructors, getters, setters...
}

// ClassroomCourse.java
@Entity
@DiscriminatorValue("CLASSROOM")
public class ClassroomCourse extends Course {
    @Column(name = "room_number")
    private String roomNumber;
    // constructors, getters, setters...
}
```

</details>

---

## Part 2: Polymorfe data opslaan en ophalen

### What you will do

Maak 2 `OnlineCourse`-objecten en 2 `ClassroomCourse`-objecten aan en sla ze op. Haal ze daarna allemaal op als `List<Course>` met een JPQL-query (`SELECT c FROM Course c`). Loop door de lijst en gebruik `instanceof` om te controleren of elk object het juiste subtype heeft. Print per cursus het type en de subtype-specifieke velden.

### Success criteria

- Er worden 4 cursussen opgeslagen: 2 online, 2 klassikaal
- Een JPQL-query `SELECT c FROM Course c` retourneert alle 4 als een `List<Course>`
- `instanceof OnlineCourse` retourneert `true` voor de online cursussen
- `instanceof ClassroomCourse` retourneert `true` voor de klassikale cursussen
- De subtype-specifieke velden (platformUrl, roomNumber) zijn beschikbaar na casting

### Hints

<details>
<summary>Hint 1</summary>

`session.persist()` werkt gewoon met subklassen. Hibernate weet op basis van het objecttype welke discriminator-waarde in de kolom moet komen.

</details>

<details>
<summary>Hint 2</summary>

In JPQL kun je `SELECT c FROM Course c` gebruiken om alle subtypes op te halen. Hibernate retourneert de juiste Java-typen — je krijgt echte `OnlineCourse` en `ClassroomCourse` objecten terug, niet gewoon `Course`.

</details>

<details>
<summary>Hint 3</summary>

Om subtype-specifieke velden te benaderen moet je casten: `if (course instanceof OnlineCourse oc) { oc.getPlatformUrl(); }`. De pattern matching syntax van Java 21 maakt dit extra leesbaar.

</details>

<details>
<summary>Hint 4</summary>

```java
// Opslaan
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    var tx = session.beginTransaction();
    session.persist(new OnlineCourse("Cloud Computing", 4, "AWS en Azure", "https://learn.example.com"));
    session.persist(new OnlineCourse("Machine Learning", 5, "ML basics", "https://ml.example.com"));
    session.persist(new ClassroomCourse("Databases", 3, "SQL fundamentals", "A1.04"));
    session.persist(new ClassroomCourse("Networking", 3, "TCP/IP", "B2.10"));
    tx.commit();
}

// Ophalen en type-checken
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    List<Course> courses = session.createQuery("SELECT c FROM Course c", Course.class).getResultList();
    for (Course c : courses) {
        if (c instanceof OnlineCourse oc) {
            System.out.println("Online: " + oc.getName() + " - " + oc.getPlatformUrl());
        } else if (c instanceof ClassroomCourse cc) {
            System.out.println("Classroom: " + cc.getName() + " - " + cc.getRoomNumber());
        }
    }
}
```

</details>

---

## Part 3: Het proxy-probleem provoceren en oplossen

### What you will do

Gebruik `session.getReference(Course.class, id)` om een cursus lazy te laden (dit geeft een proxy terug, geen echt object). Probeer `instanceof OnlineCourse` op de proxy en observeer dat het `false` retourneert, ook al is de cursus een `OnlineCourse`. Gebruik vervolgens `Hibernate.unproxy()` om het echte object te krijgen en verifieer dat `instanceof` dan wél correct werkt.

### Success criteria

- `session.getReference()` retourneert een proxy-object (niet het echte entity)
- `proxy instanceof OnlineCourse` retourneert `false` (ook al is het onderliggende object een OnlineCourse)
- `proxy.getClass().getName()` toont iets als `Course$HibernateProxy...` (niet `OnlineCourse`)
- Na `Hibernate.unproxy(proxy)` retourneert `instanceof OnlineCourse` wél `true`
- Je kunt uitleggen waarom dit gebeurt: de proxy is een subklasse van `Course`, niet van `OnlineCourse`

### Hints

<details>
<summary>Hint 1</summary>

`session.getReference()` verschilt van `session.get()`: het gaat niet meteen naar de database. In plaats daarvan maakt Hibernate een proxy-object aan dat pas data ophaalt als je een veld benadert. Het proxy-object "doet alsof" het de echte entity is, maar het is eigenlijk een door Hibernate gegenereerde subklasse.

</details>

<details>
<summary>Hint 2</summary>

Print de klasse van het proxy-object met `proxy.getClass().getName()`. Je zult zien dat het iets is als `com.example.model.Course$HibernateProxy$abc123` — het is een subklasse van `Course`, niet van `OnlineCourse`. Daarom faalt `instanceof OnlineCourse`.

</details>

<details>
<summary>Hint 3</summary>

`Hibernate.unproxy()` "pelt" de proxy-laag eraf en geeft je het echte onderliggende object. Importeer het met `import org.hibernate.Hibernate;`.

</details>

<details>
<summary>Hint 4</summary>

```java
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    // Gebruik het ID van een OnlineCourse die je eerder hebt opgeslagen
    Course proxy = session.getReference(Course.class, 1L);

    System.out.println("Klasse: " + proxy.getClass().getName());
    System.out.println("instanceof OnlineCourse? " + (proxy instanceof OnlineCourse)); // false!

    // Fix met unproxy
    Course real = (Course) Hibernate.unproxy(proxy);
    System.out.println("Na unproxy - klasse: " + real.getClass().getName());
    System.out.println("instanceof OnlineCourse? " + (real instanceof OnlineCourse)); // true!
}
```

</details>

---

## Part 4: Bonus — LazyInitializationException

### What you will do

Maak een `Department`-entity aan met een `@OneToMany`-relatie naar `Course` (of naar `OnlineCourse`/`ClassroomCourse`). Sla een department op met een paar cursussen. Haal het department op in een Session, sluit de Session, en probeer dan `department.getCourses()` aan te roepen. Vang de `LazyInitializationException` op en bedenk hoe je dit zou voorkomen.

### Success criteria

- Er is een `Department`-entity met een `@OneToMany`-relatie naar `Course`
- Een department met gekoppelde cursussen wordt opgeslagen in de database
- Het department wordt opgehaald en de Session wordt gesloten
- Het aanroepen van `getCourses()` op het detached department gooit een `LazyInitializationException`
- Je kunt minstens twee strategieën benoemen om dit te voorkomen

### Hints

<details>
<summary>Hint 1</summary>

`@OneToMany` is standaard lazy. Dat betekent dat de collectie pas wordt geladen als je er daadwerkelijk toegang toe vraagt. Als de Session op dat moment al gesloten is, kan Hibernate geen query meer uitvoeren.

</details>

<details>
<summary>Hint 2</summary>

Voor de `Department`-entity heb je een `@OneToMany(mappedBy = "department")`-annotatie nodig op een `List<Course>`. De `Course`-klasse heeft dan een `@ManyToOne`-veld `department` nodig.

</details>

<details>
<summary>Hint 3</summary>

Strategieën om `LazyInitializationException` te voorkomen:

1. De collectie ophalen binnen de Session (gewoon `department.getCourses().size()` aanroepen vóórdat je de Session sluit)
2. Een JPQL-query met `JOIN FETCH`: `SELECT d FROM Department d JOIN FETCH d.courses WHERE d.id = :id`
3. De fetch-strategie veranderen naar `FetchType.EAGER` (niet aangeraden voor productie — prestatieproblemen)

</details>

<details>
<summary>Hint 4</summary>

```java
// Department entity (minimaal)
@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Course> courses = new ArrayList<>();
    // constructors, getters, setters...
}

// Voeg aan Course toe:
@ManyToOne
@JoinColumn(name = "dept_id")
private Department department;

// De exception provoceren:
Department dept;
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    dept = session.get(Department.class, 1L);
}
// Session is gesloten!
try {
    dept.getCourses().size(); // BOEM: LazyInitializationException
} catch (org.hibernate.LazyInitializationException e) {
    System.out.println("Verwachte fout: " + e.getMessage());
}
```

</details>

---

## Reflectievragen

1. Waarom kiest Hibernate standaard voor lazy loading bij collecties? Wat zou er gebeuren als alles altijd eager geladen werd?
2. Het proxy-probleem met `instanceof` is een voorbeeld van een "leaky abstraction." Welke andere abstractie-lekken heb je vandaag gezien?
3. Welke inheritance mapping-strategie (Single Table, Table Per Class, Table Per Concrete Class) zou je kiezen als je 10 subtypes had met elk 5 unieke velden? Waarom?
4. Als je terugdenkt aan je eigen mini-ORM framework van dag 2: had jouw framework ook met het proxy-probleem te maken gehad als je overerving had geïmplementeerd?
