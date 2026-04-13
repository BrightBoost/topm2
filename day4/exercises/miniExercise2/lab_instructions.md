# Mini Exercise 2: Van UML naar code — entity-relaties in Hibernate

## Scenario / Context

Je UML-diagram van de vorige exercise ligt klaar en je teamlead is tevreden. "Mooi model," zegt ze, "maar papier draait niet in productie." Het is tijd om het diagram om te zetten naar werkende Hibernate-entiteiten. Je gaat drie entiteiten maken — `Student`, `Course` en `Enrollment` — en de relaties ertussen mappen met JPA-annotaties. Het klinkt overzichtelijk, maar de details bijten: welke kant is de "owning side"? Wat doet `mappedBy` precies? En waarom moet je bij bidirectionele relaties beide kanten handmatig synchroniseren? Je gaat het ondervinden.

---

## Learning Goals

- Drie gerelateerde Hibernate-entiteiten aanmaken met `@OneToMany` en `@ManyToOne` annotaties
- De owning side van een relatie bepalen en `mappedBy` correct toepassen op de inverse side
- Cascade-configuratie implementeren om child-entiteiten automatisch mee te persisteren via de parent
- Beide kanten van een bidirectionele relatie synchroniseren in Java-code en verklaren waarom dat nodig is
- De gegenereerde SQL analyseren om te verifiëren dat Hibernate foreign keys aanmaakt (geen join table)
- Evalueren wat er misgaat als `mappedBy` ontbreekt of als slechts één kant van de relatie wordt gezet

---

## Prerequisites

- Java 21 geïnstalleerd
- Maven geïnstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Mini-exercise 1 afgerond (je hebt een UML-diagram van het universiteitsdomein)
- Het Hibernate + H2 starter project uit dag 3 geopend
- Basiskennis van Hibernate entity mapping (`@Entity`, `@Table`, `@Id`, `@Column`)
- CRUD-operaties met Hibernate (dag 3: `session.persist()`, `session.get()`)

---

# Lab Parts

Dit lab bevat **4 delen**.

---

## Part 1: De drie entiteiten aanmaken

### What you will do

Maak drie entity-klassen aan in je project: `Student`, `Course` en `Enrollment`. Elk met de juiste JPA-annotaties (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`). Voeg de attributen toe maar laat de relatie-velden nog even weg — die komen in Part 2. Registreer alle drie in `HibernateUtil` en start de applicatie om te verifiëren dat de tabellen worden aangemaakt.

### Success criteria

- Er zijn drie entity-klassen: `Student`, `Course`, `Enrollment`
- Elke klasse heeft `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
- `Student` heeft velden: `name`, `studentNumber`, `email`
- `Course` heeft velden: `name`, `credits`, `description`
- `Enrollment` heeft velden: `enrollmentDate` (LocalDate), `grade` (String, nullable)
- Alle drie zijn geregistreerd in `HibernateUtil` met `addAnnotatedClass`
- Bij het starten verschijnen drie `CREATE TABLE`-statements in de console

### Hints

<details>
<summary>Hint 1</summary>

Begin met de simpelste entiteit — bijv. `Course`. Je hebt deze op dag 3 al eens gemaakt. Kopieer het patroon: `@Entity`, `@Table(name = "...")`, `@Id` met `@GeneratedValue`, en de velden. Vergeet de no-arg constructor niet!

</details>

<details>
<summary>Hint 2</summary>

Voor `enrollmentDate` gebruik je `java.time.LocalDate`. Hibernate mapt dit automatisch naar een SQL `DATE`-kolom. Je hoeft geen `@Column` toe te voegen tenzij je de kolomnaam wilt aanpassen.

</details>

<details>
<summary>Hint 3</summary>

Registreer alle drie de klassen in `HibernateUtil`:

```java
configuration.addAnnotatedClass(Student.class);
configuration.addAnnotatedClass(Course.class);
configuration.addAnnotatedClass(Enrollment.class);
```

</details>

<details>
<summary>Hint 4</summary>

```java
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "student_number")
    private String studentNumber;
    private String email;

    public Student() {}
    public Student(String name, String studentNumber, String email) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.email = email;
    }
    // getters en setters...
}
```

Pas hetzelfde patroon toe voor `Course` en `Enrollment`.

</details>

---

## Part 2: Relaties mappen met annotaties

### What you will do

Voeg de relatie-velden toe aan je entiteiten. `Enrollment` krijgt een `@ManyToOne`-referentie naar `Student` en naar `Course` — dit is de owning side (hier zitten de foreign keys). `Student` en `Course` krijgen elk een `@OneToMany`-collectie van enrollments, met `mappedBy` om aan te geven dat zij niet de foreign key beheren. Voeg `CascadeType.PERSIST` toe op `Student.enrollments`.

### Success criteria voor voorbeeld diagram

- `Enrollment` heeft `@ManyToOne` velden `student` en `course` met `@JoinColumn`
- `Student` heeft een `@OneToMany(mappedBy = "student")` collectie van `Enrollment`
- `Course` heeft een `@OneToMany(mappedBy = "course")` collectie van `Enrollment`
- `Student.enrollments` heeft `cascade = CascadeType.PERSIST`
- Bij het starten toont de console dat de `enrollments`-tabel foreign keys heeft naar `students` en `courses`
- Er wordt **geen** join table aangemaakt (geen `student_enrollments` of vergelijkbare tabel)

### Hints

<details>
<summary>Hint 1</summary>

De owning side is de kant met de foreign key — dat is bijna altijd de "many"-kant. `Enrollment` heeft veel enrollments per student, dus `Enrollment` is de owning side met `@ManyToOne` en `@JoinColumn`.

</details>

<details>
<summary>Hint 2</summary>

`mappedBy` vertelt Hibernate: "De andere kant beheert deze relatie in de database. Ik maak geen eigen foreign key of join table aan." De waarde van `mappedBy` is de veldnaam in de andere klasse — niet de kolomnaam. Dus `@OneToMany(mappedBy = "student")` verwijst naar het veld `student` in `Enrollment`.

</details>

<details>
<summary>Hint 3</summary>

Als je `mappedBy` vergeet, maakt Hibernate automatisch een join table aan (bijv. `students_enrollments`). Dat is bijna nooit wat je wilt bij een one-to-many. Probeer het even zonder `mappedBy` om te zien wat er gebeurt, en voeg het dan toe.

</details>

<details>
<summary>Hint 4</summary>

```java
// In Enrollment.java
@ManyToOne
@JoinColumn(name = "student_id")
private Student student;

@ManyToOne
@JoinColumn(name = "course_id")
private Course course;

// In Student.java
@OneToMany(mappedBy = "student", cascade = CascadeType.PERSIST)
private List<Enrollment> enrollments = new ArrayList<>();

// In Course.java
@OneToMany(mappedBy = "course")
private List<Enrollment> enrollments = new ArrayList<>();
```

</details>

---

## Part 3: Data aanmaken en persisteren met cascade

### What you will do

Schrijf code in een `main`-methode die een student aanmaakt, twee cursussen aanmaakt, en de student inschrijft voor beide cursussen. Cursussen moeten apart gepersisteerd worden (geen cascade). Enrollments worden gecascade via de student. Let op: je moet beide kanten van de relatie zetten — zowel de enrollment aan de student toevoegen, als de student op de enrollment zetten.

### Success criteria

- Twee cursussen worden apart gepersisteerd in een eigen transactie
- Een student wordt aangemaakt met twee enrollments
- De enrollments worden automatisch gepersisteerd door `CascadeType.PERSIST` op de student
- In de console verschijnen INSERT-statements voor student en enrollments (maar niet opnieuw voor courses)
- Na heropenen van een nieuwe Session bevatten de enrollments de juiste referenties naar student en course

### Hints

<details>
<summary>Hint 1</summary>

Persisteer de cursussen eerst, in een eigen Session/transactie. Ze bestaan onafhankelijk van studenten — zonder cascade. Pas daarna maak je de student en enrollments aan.

</details>

<details>
<summary>Hint 2</summary>

Het synchroniseren van beide kanten is cruciaal. Hibernate kijkt alleen naar de owning side (`Enrollment.student`) voor de database. Maar als je `student.getEnrollments()` niet bijwerkt, is je Java object-graph inconsistent. Zet altijd beide kanten.

</details>

<details>
<summary>Hint 3</summary>

Wat als je alleen `student.getEnrollments().add(enrollment)` doet maar niet `enrollment.setStudent(student)`? Dan heeft de owning side geen referentie, en Hibernate slaat de foreign key niet op. De enrollment komt in de database terecht met `student_id = NULL`.

</details>

<details>
<summary>Hint 4</summary>

```java
// Stap 1: Cursussen aanmaken en opslaan
Course databases, webdev;
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    var tx = session.beginTransaction();
    databases = new Course("Databases", 3, "SQL en relationele databases");
    webdev = new Course("Web Development", 4, "Frontend en backend development");
    session.persist(databases);
    session.persist(webdev);
    tx.commit();
}

// Stap 2: Student met enrollments (cascade)
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    var tx = session.beginTransaction();

    // Cursussen opnieuw ophalen in deze Session
    databases = session.get(Course.class, databases.getId());
    webdev = session.get(Course.class, webdev.getId());

    Student alice = new Student("Alice", "S12345", "alice@university.nl");

    Enrollment e1 = new Enrollment(LocalDate.now(), null);
    e1.setStudent(alice);       // owning side
    e1.setCourse(databases);
    alice.getEnrollments().add(e1); // inverse side

    Enrollment e2 = new Enrollment(LocalDate.now(), null);
    e2.setStudent(alice);
    e2.setCourse(webdev);
    alice.getEnrollments().add(e2);

    session.persist(alice); // cascades naar e1 en e2
    tx.commit();
}

// Stap 3: Verifiëren
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    var enrollments = session.createQuery(
        "SELECT e FROM Enrollment e", Enrollment.class).getResultList();
    for (var e : enrollments) {
        System.out.println(e.getStudent().getName()
            + " → " + e.getCourse().getName());
    }
}
```

</details>

---

## Part 4: De gegenereerde SQL analyseren

### What you will do

Bekijk de SQL-output in de console (dankzij `hibernate.show_sql=true`). Identificeer de `CREATE TABLE`-statements en let op de foreign keys. Bekijk de INSERT-volgorde: in welke volgorde persisteert Hibernate de objecten? Begrijp waarom die volgorde er toe doet.

### Success criteria

- De `enrollments`-tabel heeft foreign key kolommen `student_id` en `course_id`
- De INSERT-volgorde is logisch: eerst de student, dan de enrollments (de student moet bestaan voordat de FK ernaar kan verwijzen)
- Er is **geen** join table aangemaakt (geen `students_enrollments`)
- Je kunt uitleggen waarom Hibernate de student vóór de enrollments moet persisteren

### Hints

<details>
<summary>Hint 1</summary>

Zoek in de console-output naar `create table enrollments`. Je zou kolommen `student_id` en `course_id` moeten zien, met foreign key constraints.

</details>

<details>
<summary>Hint 2</summary>

Hibernate moet de owning side (enrollments) als laatste inserten, want de foreign key verwijst naar de student en course die al moeten bestaan. Dit is vergelijkbaar met hoe je in SQL ook eerst de parent-tabellen vult.

</details>

<details>
<summary>Hint 3</summary>

Als je wél een join table ziet (bijv. `students_enrollments`), dan ontbreekt waarschijnlijk `mappedBy` in je `@OneToMany`. Dit is de meest voorkomende fout. Voeg `mappedBy = "student"` toe en start opnieuw.

</details>

---

# Bonus Challenge (Optional)

Maak een convenience method `student.enroll(Course course)` die in één aanroep:

1. Een nieuwe `Enrollment` aanmaakt met de datum van vandaag
2. De student op de enrollment zet (owning side)
3. De enrollment aan de student's lijst toevoegt (inverse side)
4. De enrollment retourneert

Test de methode door Alice in te schrijven voor een derde cursus met `alice.enroll(thirdCourse)` en verifieer dat het werkt. Denk na: waarom is zo'n helper-methode een goed idee in productie-code?

---

# Reflectievragen

### Implementatie & Trade-offs

1. Waarom is `Enrollment` de owning side van de relatie en niet `Student`? Wat zou er veranderen als je de owning side omdraait?
2. Je hebt `CascadeType.PERSIST` alleen op `Student.enrollments` gezet, niet op `Course.enrollments`. Waarom is dat een bewuste keuze? Wat zou er misgaan met `CascadeType.ALL` op de course-kant?

### Production Readiness

3. In een echte applicatie met duizenden studenten en cursussen: welke problemen voorzie je als je `CascadeType.REMOVE` zou toevoegen aan `Student.enrollments`? Wat als een student per ongeluk wordt verwijderd?
4. Hoe zou je ervoor zorgen dat een student zich niet twee keer voor dezelfde cursus kan inschrijven? Waar in de code of database zou je die constraint leggen?

### Debugging & Problem Solving

5. Wat gebeurt er als je alleen `student.getEnrollments().add(enrollment)` aanroept maar niet `enrollment.setStudent(student)`? Beschrijf het concrete resultaat in de database.
6. Als je `mappedBy` vergeet op de `@OneToMany` in `Student`, welk extra SQL-statement genereert Hibernate dan, en waarom is dat een probleem?

### Adaptatie / Transfer

7. Je hebt nu een one-to-many via een join-entiteit geïmplementeerd. Als je een directe `@ManyToMany` tussen `Student` en `Course` had gebruikt (zonder `Enrollment`-entiteit), hoe zou je dan later een cijfer per inschrijving toevoegen?
8. Stel dat je dezelfde relaties moet modelleren in een microservices-architectuur waar `Student` en `Course` in aparte services leven. Hoe zou je de "enrollment"-relatie dan implementeren zonder gedeelde database?

