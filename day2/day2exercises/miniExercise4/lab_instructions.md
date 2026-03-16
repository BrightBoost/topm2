# Lab: Optimistic Locking in Action

## Scenario / Context

Het studentenadministratiesysteem wordt inmiddels door meerdere medewerkers tegelijk gebruikt. Twee studieadviseurs openen allebei het profiel van dezelfde student, wijzigen ieder een ander veld, en klikken op "opslaan". Zonder bescherming overschrijft de tweede opslag de wijziging van de eerste — een lost update, en niemand die het doorheeft. Dit is precies het probleem dat je in de theorie hebt gezien. Jouw opdracht: implementeer optimistic locking met een `version`-kolom, zodat de tweede opslag een conflict detecteert in plaats van stiekem data te overschrijven.

---

## Learning Goals

- Een `version`-kolom toevoegen aan een bestaande tabel en begrijpen hoe deze conflict-detectie mogelijk maakt
- Een UPDATE-statement implementeren dat de versie controleert in de WHERE-clause en verhoogt in de SET-clause
- Een conflict simuleren door een update uit te voeren met een verouderd versienummer en het resultaat interpreteren
- Beredeneren waarom optimistic locking beter schaalt dan pessimistic locking voor de meeste webapplicaties
- Evalueren hoe je als applicatie moet reageren wanneer `executeUpdate()` 0 rijen retourneert

---

## Prerequisites

- Java 21 geinstalleerd
- Maven geinstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Basiskennis JDBC met transacties (mini-exercises 1 en 2 afgerond)
- Kennis van isolation levels (mini-exercise 3 doorgenomen)
- De slides over optimistic en pessimistic locking bekeken

---

# Lab Parts

Dit lab bevat **4 delen**.

---

## Part 1: Tabel opzetten met een version-kolom

### What you will do

Maak een `students`-tabel aan met een extra `version`-kolom (type `INT`, default waarde `1`) (of pas de oude tabel aan). Voeg een student toe en controleer dat de version-kolom correct wordt gevuld.

### Success criteria

- De `students`-tabel heeft kolommen `id`, `name`, `email`, `age` en `version`
- De `version`-kolom heeft een default waarde van `1`
- Na een INSERT zonder expliciete version-waarde staat er `1` in de version-kolom
- Een SELECT bevestigt dat de student correct is ingevoegd met version = 1

### Hints

<details>
<summary>Hint 1</summary>

Je kunt je bestaande project hergebruiken. Het enige verschil is de extra kolom in je CREATE TABLE-statement.

</details>

<details>
<summary>Hint 2</summary>

Gebruik `DEFAULT 1` bij de version-kolom zodat je bij een INSERT de version niet expliciet hoeft mee te geven.

</details>

<details>
<summary>Hint 3</summary>

```sql
DROP TABLE IF EXISTS students;
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    age INT,
    version INT DEFAULT 1
);
```

Voeg daarna een student toe.

---

## Part 2: Update met versiecontrole

### What you will do

Lees de student uit de database (inclusief de version), en voer een UPDATE uit die de version controleert in de WHERE-clause en met 1 verhoogt in de SET-clause. Controleer dat de update slaagt en dat de version in de database nu 2 is.

### Success criteria

- De student wordt gelezen met version = 1
- Het UPDATE-statement bevat `AND version = ?` in de WHERE-clause
- Het UPDATE-statement bevat `version = version + 1` in de SET-clause
- `executeUpdate()` retourneert `1` (precies 1 rij aangepast)
- Een SELECT bevestigt dat de version nu `2` is

### Hints

<details>
<summary>Hint 1</summary>

Lees eerst de student op met een SELECT. Sla de version op in een variabele — je hebt die nodig voor de WHERE-clause van je UPDATE.

</details>

<details>
<summary>Hint 2</summary>

Het UPDATE-patroon is: wijzig de velden die je wilt veranderen, verhoog de version, en eis dat zowel `id` als `version` matchen in de WHERE.

</details>

<details>
<summary>Hint 3</summary>

```java
// Lees de student
String selectSql = "SELECT id, name, email, age, version FROM students WHERE id = ?";
// sla id, name, age, version op in variabelen

// Update met versiecheck
String updateSql = "UPDATE students SET name = ?, age = ?, version = version + 1 "
                 + "WHERE id = ? AND version = ?";
try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
    ps.setString(1, "Jan de Vries"); // nieuwe naam
    ps.setInt(2, 23);                // nieuwe leeftijd
    ps.setLong(3, studentId);
    ps.setInt(4, currentVersion);    // version die we gelezen hebben
    int rowsUpdated = ps.executeUpdate();
    System.out.println("Rows updated: " + rowsUpdated);
}
```

</details>

---

## Part 3: Conflict simuleren

### What you will do

Simuleer een conflict: voer een tweede UPDATE uit met dezelfde (nu verouderde) version. Controleer dat `executeUpdate()` nu 0 retourneert — het conflict is gedetecteerd.

### Success criteria

- De tweede UPDATE gebruikt het oude versienummer (version = 1, terwijl de database nu version = 2 heeft)
- `executeUpdate()` retourneert `0`
- Je code detecteert dit en print een duidelijke melding dat er een conflict is
- De data in de database is niet gewijzigd door de tweede update

### Hints

<details>
<summary>Hint 1</summary>

Stel je voor dat een tweede gebruiker het profiel opende op het moment dat version nog 1 was. Die gebruiker probeert nu op te slaan terwijl de eerste gebruiker al heeft gecommit. De version in de database is inmiddels 2, maar de tweede gebruiker stuurt version 1 mee.

</details>

<details>
<summary>Hint 2</summary>

Hergebruik dezelfde UPDATE-query als in Part 2, maar vul de oude version in:

```java
ps.setInt(4, 1); // oude version, database heeft inmiddels 2
```

</details>

<details>
<summary>Hint 3</summary>

De returnwaarde van `executeUpdate()` vertelt je alles. Controleer het resultaat:

```java
int rowsUpdated = ps.executeUpdate();
if (rowsUpdated == 0) {
    System.out.println("CONFLICT: data is gewijzigd door een andere gebruiker!");
    // in een echte applicatie: throw new OptimisticLockException(...)
}
```

</details>

<details>
<summary>Hint 4</summary>

Voer na de gefaalde update een SELECT uit om te bevestigen dat de data nog steeds de waarden van de eerste update bevat (name = "Jan de Vries", version = 2). De "stiekeme overschrijving" is voorkomen.

</details>

---

## Part 4: Conflict afhandeling

### What you will do

Maak de conflict-detectie robuuster: gooi een exception wanneer 0 rijen zijn geüpdatet, en schrijf code die het conflict afhandelt door de student opnieuw in te lezen en de nieuwe version te tonen als output.

### Success criteria

- Bij 0 affected rows wordt een exception gegooid (bijv. een eigen `OptimisticLockException` of een `RuntimeException` met duidelijke melding)
- De catch-block leest de student opnieuw uit de database
- De console toont de huidige versie en data uit de database
- Het programma crasht niet — het conflict wordt netjes afgehandeld

### Hints

<details>
<summary>Hint 1</summary>

In een echte applicatie zou je op dit punt de gebruiker informeren: "Iemand anders heeft deze data gewijzigd. Wil je de wijzigingen opnieuw bekijken?" Voor deze oefening is een console-melding voldoende.

</details>

<details>
<summary>Hint 2</summary>

Maak een eigen exception-klasse, of gebruik een `RuntimeException`:

```java
if (rowsUpdated == 0) {
    throw new RuntimeException("Optimistic lock conflict: student was modified");
}
```

</details>

<details>
<summary>Hint 3</summary>

In de catch-block, lees de student opnieuw in:

```java
try {
    updateStudent(conn, studentId, "Andere Naam", 25, 1); // oude version
} catch (RuntimeException e) {
    System.out.println(e.getMessage());
    System.out.println("Huidige staat in de database:");
    Student freshStudent = readStudent(conn, studentId);
    System.out.println("  Naam: " + freshStudent.name
        + ", Version: " + freshStudent.version);
}
```

</details>

---

# Bonus Challenge (Optioneel)

Implementeer een retry-mechanisme: wanneer een optimistic lock conflict optreedt, lees de student opnieuw in (met de nieuwe version), pas je wijziging toe op de verse data, en probeer de update opnieuw. Beperk het tot maximaal 3 pogingen om een oneindige loop te voorkomen. Log elke poging naar de console. Bedenk: in welke situaties zou zelfs een retry niet helpen?

---

# Reflectievragen

### Implementatie & Afwegingen

- De version-kolom is een `INT`. Wat gebeurt er als een rij heel vaak wordt geüpdatet — kan de version ooit een probleem worden? Hoe zou je dat oplossen?
- Je controleert het versienummer in de WHERE-clause. Waarom is het niet voldoende om alleen op `id` te filteren en achteraf de version te vergelijken in Java?

### Production Readiness

- In een webapplicatie zit er soms minuten tussen het lezen van de data en het versturen van de update. Hoe bewaart de applicatie de version in die tussentijd (denk aan HTTP, sessions, formulieren)?
- Wat gebeurt er als je `version = version + 1` vergeet in je SET-clause maar de version-check wél in de WHERE hebt? Hoe zou je dit soort fouten voorkomen in een team?

### Debugging & Problem Solving

- Je update retourneert 0 rijen, maar je weet niet of dat komt door een verkeerd `id` of een version-mismatch. Hoe zou je onderscheid maken tussen deze twee situaties?
- Een collega meldt dat optimistic locking "niet werkt" in zijn code — updates slagen altijd, ook bij conflicten. Wat controleer je als eerste?

### Aanpassing / Transfer

- Frameworks zoals Hibernate gebruiken een `@Version`-annotatie die dit hele mechanisme automatisch afhandelt. Wat zijn de voor- en nadelen van het handmatig implementeren versus het aan een framework overlaten?
- Bedenk een scenario waarin optimistic locking niet geschikt is en je pessimistic locking (`SELECT ... FOR UPDATE`) zou moeten gebruiken. Wat maakt dat scenario anders?
