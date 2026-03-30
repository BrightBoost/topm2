# Mini Exercise: CRUD & JPQL

- Use the `Course` entity from the previous exercise (or the demo `Student` entity)
- **Create**: persist 3 courses
- **Read**: retrieve one course with `session.get()`
- **Update**: change a course name (without calling save!) and commit — observe dirty checking
- **Delete**: remove one course
- Turn on `hibernate.show_sql=true` and examine the generated SQL for each operation
- Write a JPQL query: find all courses with more than 3 credits
- Experiment: modify a field on a persistent object and commit — does Hibernate generate an UPDATE without you asking?
- **Bonus question**: what happens if you modify a field on a detached object (outside the Session)? Try it and explain the result

---

# Lab: CRUD en JPQL met Hibernate

## Scenario / Context

Je proof of concept uit de vorige exercise was een succes: de `Course`-entity is gemapt en Hibernate maakt de tabel automatisch aan. Je teamlead is onder de indruk, maar wil nu het echte werk zien. Ze wil dat je de volledige CRUD-cyclus implementeert met Hibernate: cursussen aanmaken, ophalen, wijzigen en verwijderen. Daarnaast wil ze een overzicht van alle cursussen met meer dan 3 studiepunten — via JPQL in plaats van handmatige SQL. Oh, en ze heeft gehoord over iets dat "dirty checking" heet en wil dat je uitlegt hoe dat werkt. Tijd om Hibernate echt aan het werk te zetten.

---

## Learning Goals

- Objecten opslaan in de database met `session.persist()` en de gegenereerde INSERT-SQL analyseren
- Objecten ophalen met `session.get()` en het verschil met raw JDBC ResultSet-mapping evalueren
- Dirty checking in actie observeren: een veld wijzigen zonder expliciete save-aanroep en verifiëren dat Hibernate automatisch een UPDATE genereert
- Objecten verwijderen met `session.remove()` en de gegenereerde DELETE-SQL bekijken
- Een JPQL-query schrijven met named parameters en het verschil met SQL begrijpen
- Het gedrag van detached objecten onderzoeken en beredeneren waarom wijzigingen buiten een Session niet worden opgeslagen

---

## Prerequisites

- Java 21 geïnstalleerd
- Maven geïnstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Mini-exercise 1 afgerond (je hebt een werkende `Course`-entity en `HibernateUtil`)
- Basiskennis van Hibernate entity mapping (`@Entity`, `@Table`, `@Id`, `@Column`)

---

# Lab Parts

Dit lab bevat **5 delen**.

---

## Part 1: Cursussen aanmaken (Create)

### What you will do

Maak drie `Course`-objecten aan en sla ze op in de database met `session.persist()`. Gebruik een `Session` en een `Transaction`. Controleer in de console dat Hibernate drie INSERT-statements genereert. Verifieer daarna dat de objecten een automatisch gegenereerd `id` hebben gekregen.

### Success criteria

- Er worden drie cursussen aangemaakt met verschillende namen, credits en beschrijvingen
- Alle drie worden opgeslagen via `session.persist()` binnen een transactie
- In de console verschijnen drie `INSERT INTO courses`-statements
- Na `persist()` heeft elk `Course`-object een `id` dat niet `null` is
- De transactie wordt gecommit zonder fouten

### Hints

<details>
<summary>Hint 1</summary>

Alle schrijfoperaties in Hibernate moeten binnen een transactie plaatsvinden. Open een `Session`, begin een `Transaction`, doe je werk, en commit.

</details>

<details>
<summary>Hint 2</summary>

Het patroon ziet er zo uit: open een session, begin een transactie, persist je objecten, commit, en sluit de session. Gebruik try-with-resources voor de session.

</details>

<details>
<summary>Hint 3</summary>

Na `session.persist(course)` kun je `course.getId()` aanroepen. Als het ID niet `null` is, weet je dat Hibernate het INSERT-statement al heeft uitgevoerd en het gegenereerde ID heeft ingevuld.

</details>

<details>
<summary>Hint 4</summary>

```java
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    var tx = session.beginTransaction();

    Course c1 = new Course("Java Fundamentals", 5, "Introductie tot Java");
    Course c2 = new Course("Database Design", 4, "Relationele databases ontwerpen");
    Course c3 = new Course("Web Development", 2, "HTML, CSS en JavaScript");

    session.persist(c1);
    session.persist(c2);
    session.persist(c3);

    System.out.println("IDs: " + c1.getId() + ", " + c2.getId() + ", " + c3.getId());

    tx.commit();
}
```

</details>

---

## Part 2: Een cursus ophalen (Read)

### What you will do

Open een nieuwe `Session` en haal een cursus op met `session.get(Course.class, id)`. Print de gegevens van de opgehaalde cursus. Probeer ook een cursus op te halen met een ID dat niet bestaat en controleer wat je terugkrijgt.

### Success criteria

- Een cursus wordt succesvol opgehaald met `session.get()` en de juiste gegevens worden geprint
- In de console verschijnt een `SELECT`-statement
- Het ophalen van een niet-bestaand ID retourneert `null` (geen exception)

### Hints

<details>
<summary>Hint 1</summary>

`session.get()` heeft twee parameters nodig: de entity-klasse en het ID. Het retourneert het object of `null` als het niet bestaat.

</details>

<details>
<summary>Hint 2</summary>

Je hebt geen transactie nodig voor read-only operaties (het kan wel, maar het is niet verplicht).

</details>

<details>
<summary>Hint 3</summary>

```java
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    Course course = session.get(Course.class, 1L);
    System.out.println("Gevonden: " + course);

    Course missing = session.get(Course.class, 999L);
    System.out.println("Niet gevonden: " + missing); // null
}
```

</details>

---

## Part 3: Een cursus wijzigen (Update) — dirty checking

### What you will do

Haal een bestaande cursus op binnen een `Session` met een actieve transactie. Wijzig de naam van de cursus via de setter (`course.setName("Nieuwe naam")`). Doe **niets** anders — geen `save()`, geen `update()`, geen `merge()`. Commit de transactie en controleer of Hibernate automatisch een UPDATE-statement heeft gegenereerd. Dit is dirty checking in actie.

### Success criteria

- Een cursus wordt opgehaald binnen een transactie
- De naam wordt gewijzigd via de setter
- Er wordt **geen** expliciete save/update-methode aangeroepen
- Bij commit verschijnt er een `UPDATE courses`-statement in de console
- Na heropenen van een nieuwe Session bevat de cursus de nieuwe naam

### Hints

<details>
<summary>Hint 1</summary>

Dirty checking betekent dat Hibernate bij elke commit controleert of er velden zijn gewijzigd op persistent objecten. Zo ja, dan genereert het automatisch een UPDATE. Je hoeft zelf niets te doen.

</details>

<details>
<summary>Hint 2</summary>

Het object moet "persistent" zijn — dat wil zeggen: opgehaald via een actieve Session. Als het object "detached" is (de Session is al gesloten), werkt dirty checking niet.

</details>

<details>
<summary>Hint 3</summary>

Haal de cursus op, wijzig het veld, commit. Open daarna een nieuwe Session om te verifiëren dat de wijziging in de database staat.

</details>

<details>
<summary>Hint 4</summary>

```java
// Wijzigen
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    var tx = session.beginTransaction();
    Course course = session.get(Course.class, 1L);
    course.setName("Advanced Java"); // alleen de setter aanroepen!
    tx.commit(); // Hibernate detecteert de wijziging en genereert UPDATE
}

// Verifiëren
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    Course course = session.get(Course.class, 1L);
    System.out.println("Nieuwe naam: " + course.getName()); // "Advanced Java"
}
```

</details>

---

## Part 4: Een cursus verwijderen (Delete) en JPQL query

### What you will do

Verwijder een cursus met `session.remove()` binnen een transactie. Schrijf daarna een JPQL-query die alle cursussen ophaalt met meer dan 3 credits: `SELECT c FROM Course c WHERE c.credits > :minCredits`. Let op: JPQL gebruikt de **entity-naam** en **veldnamen**, niet de tabelnaam en kolomnamen.

### Success criteria

- Een cursus wordt verwijderd via `session.remove()` en er verschijnt een `DELETE`-statement in de console
- De JPQL-query retourneert alleen cursussen met meer dan 3 credits
- De query gebruikt named parameters (`:minCredits`), geen hardcoded waarden
- Je kunt uitleggen waarom JPQL `Course` (met hoofdletter) gebruikt in plaats van `courses` (de tabelnaam)

### Hints

<details>
<summary>Hint 1</summary>

Om te verwijderen moet je het object eerst ophalen (of een referentie hebben). Haal het op met `session.get()`, roep dan `session.remove()` aan, en commit.

</details>

<details>
<summary>Hint 2</summary>

JPQL werkt met entity-klassen, niet met tabellen. `SELECT c FROM Course c` verwijst naar de Java-klasse `Course`, niet naar de tabel `courses`. Veldnamen in JPQL komen overeen met Java-veldnamen (`c.credits`), niet met kolomnamen.

</details>

<details>
<summary>Hint 3</summary>

Gebruik `session.createQuery()` met de JPQL-string en het verwachte resultaattype. Stel de parameter in met `.setParameter("minCredits", 3)`.

</details>

<details>
<summary>Hint 4</summary>

```java
// Delete
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    var tx = session.beginTransaction();
    Course course = session.get(Course.class, 3L);
    session.remove(course);
    tx.commit();
}

// JPQL query
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    List<Course> courses = session.createQuery(
            "SELECT c FROM Course c WHERE c.credits > :minCredits", Course.class)
        .setParameter("minCredits", 3)
        .getResultList();

    courses.forEach(c -> System.out.println(c.getName() + " (" + c.getCredits() + " credits)"));
}
```

</details>

---

## Part 5: Bonus — detached objecten

### What you will do

Onderzoek wat er gebeurt met een detached object. Haal een cursus op in een Session, sluit de Session, wijzig de naam van het object, en open een nieuwe Session. Controleer of de wijziging in de database staat. Beredeneer waarom dit al dan niet werkt.

### Success criteria

- Een cursus wordt opgehaald en de Session wordt gesloten
- De naam van het (nu detached) object wordt gewijzigd
- Na het openen van een nieuwe Session blijkt dat de wijziging **niet** in de database staat
- Je kunt uitleggen dat een detached object niet meer wordt gevolgd door Hibernate (geen dirty checking)

### Hints

<details>
<summary>Hint 1</summary>

Een object wordt "detached" zodra de Session waarmee het werd opgehaald gesloten is. Hibernate houdt het dan niet meer in de gaten.

</details>

<details>
<summary>Hint 2</summary>

Dit is vergelijkbaar met het verschil tussen een document dat geopend is in een editor (persistent — wijzigingen worden bijgehouden) en een document dat je hebt gekopieerd naar je bureaublad (detached — wijzigingen op de kopie worden niet teruggeschreven).

</details>

<details>
<summary>Hint 3</summary>

Als je toch een detached object wilt opslaan, kun je `session.merge(detachedObject)` gebruiken in een nieuwe Session. Maar dat is een bewuste actie — het gebeurt niet automatisch.

</details>

<details>
<summary>Hint 4</summary>

```java
Course detachedCourse;
// Ophalen en Session sluiten
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    detachedCourse = session.get(Course.class, 1L);
}
// Session is gesloten — object is nu detached
detachedCourse.setName("Dit wordt NIET opgeslagen");

// Nieuwe Session — controleer de database
try (var session = HibernateUtil.getSessionFactory().openSession()) {
    Course fromDb = session.get(Course.class, 1L);
    System.out.println("Naam in DB: " + fromDb.getName()); // nog steeds de oude naam!
}
```

</details>

---

## Reflectievragen

1. Wat vind je van dirty checking? Is het handig of gevaarlijk (of allebei)?
2. Wat zou er gebeuren als je per ongeluk een veld wijzigt op een persistent object en dan commit? Hoe zou je dat voorkomen?
3. Hoeveel regels code heb je nodig voor een JPQL-query vergeleken met dezelfde query in raw JDBC? Wat wint Hibernate hier precies voor je?
4. In welke situaties zou je toch raw SQL willen gebruiken in plaats van JPQL?
