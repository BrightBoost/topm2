# Lab: Eerste CRUD operaties met JDBC

## Scenario

Je bent net begonnen als developer bij een onderwijsinstelling. De administratie draait nog op Excel-bestanden en het is jouw taak om de allereerste stap te zetten richting een echte database-oplossing. Je begint klein: een simpele `students`-tabel aanmaken en daar data in beheren via Java en JDBC. Het klinkt misschien bescheiden, maar zonder deze basis komt er nooit een werkend systeem.

---

## Learning Goals

- Een JDBC-connectie opzetten naar een H2 file-based database en SQL-statements uitvoeren vanuit Java
- CRUD-operaties (Create, Read, Update, Delete) implementeren met `Statement` en SQL
-  Het resultaat van elke operatie verifiëren door affected rows en query-resultaten te interpreteren
- Beoordelen of de uitgevoerde operaties correct zijn door de data in de database te inspecteren via console-output

---

## Prerequisites

- Java 21 geïnstalleerd
- Maven geïnstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Basiskennis SQL (CREATE TABLE, INSERT, SELECT, UPDATE, DELETE)
- Basiskennis Java (classes, methodes, try-catch)

---

# Lab Parts

Dit lab bevat **5 delen** die je stap voor stap door het CRUD-proces leiden.

---

## Part 1: Connectie maken en tabel aanmaken

### What you will do

Maak een JDBC-connectie naar een H2 file-based database en maak de `students`-tabel aan met kolommen `id` (auto-increment), `name`, `email` en `age`.

### Success criteria

- De applicatie start zonder fouten
- In de console verschijnt een bevestiging dat de tabel is aangemaakt
- De tabel bestaat in je database

### Hints

<details>
<summary>Hint 1</summary>

Je hebt een JDBC URL nodig die naar een lokaal bestand wijst. H2 ondersteunt dit met `jdbc:h2:file:./pad/naar/bestand`.

</details>

<details>
<summary>Hint 2</summary>

Gebruik `DriverManager.getConnection(url, user, password)` om een connectie te openen. Voor H2 is de standaard user `"sa"` met een leeg wachtwoord.

</details>

<details>
<summary>Hint 3</summary>

Gebruik `CREATE TABLE IF NOT EXISTS` zodat je programma meerdere keren kan draaien zonder errors.

</details>

<details>
<summary>Hint 4</summary>

```java
try (Connection conn = DriverManager.getConnection(url, user, password)) {
    try (Statement stmt = conn.createStatement()) {
        stmt.execute("CREATE TABLE IF NOT EXISTS students (...)");
    }
}
```

</details>

---

## Part 2: Studenten toevoegen

### What you will do

Voeg 3 studenten toe aan de `students`-tabel met INSERT-statements. Print na elke insert het aantal affected rows naar de console.

### Success criteria

- 3 studenten zijn toegevoegd aan de database
- Na elke INSERT wordt `1` geprint als affected rows
- Geen duplicate-fouten bij het invoegen

### Hints

<details>
<summary>Hint 1</summary>

`Statement.executeUpdate()` retourneert het aantal rijen dat is aangepast. Voor een INSERT is dat normaal `1`.

</details>

<details>
<summary>Hint 2</summary>

Schrijf volledige INSERT-statements met alle kolommen behalve `id` (die is `AUTO_INCREMENT`).

</details>

<details>
<summary>Hint 3</summary>

```java
String sql = "INSERT INTO students (name, email, age) VALUES ('Jan', 'jan@email.nl', 22)";
int rows = stmt.executeUpdate(sql);
System.out.println("Rows affected: " + rows);
```

</details>

---

## Part 3: Alle studenten ophalen

### What you will do

Haal alle studenten op uit de database met een SELECT-query en print ze naar de console in een leesbaar formaat.

### Success criteria

- Alle 3 de studenten verschijnen in de console-output
- Voor elke student worden id, name, email en age getoond

### Hints

<details>
<summary>Hint 1</summary>

`Statement.executeQuery()` geeft een `ResultSet` terug. Je moet door dit resultaat heen loopen.

</details>

<details>
<summary>Hint 2</summary>

Gebruik `rs.next()` in een while-loop om door de resultaten te itereren.

</details>

<details>
<summary>Hint 3</summary>

Gebruik `rs.getLong("id")`, `rs.getString("name")`, `rs.getString("email")` en `rs.getInt("age")` om de waarden op te halen.

</details>

<details>
<summary>Hint 4</summary>

```java
ResultSet rs = stmt.executeQuery("SELECT * FROM students");
while (rs.next()) {
    System.out.println(rs.getLong("id") + " | " + rs.getString("name") + " | " + ...);
}
```

</details>

---

## Part 4: Student updaten

### What you will do

Update de leeftijd van een van de studenten. Print het aantal affected rows en haal daarna alle studenten opnieuw op om te verifiëren dat de update is doorgevoerd.

### Success criteria

- `executeUpdate()` retourneert `1`
- Bij het opnieuw ophalen van alle studenten is de leeftijd van de betreffende student gewijzigd

### Hints

<details>
<summary>Hint 1</summary>

Gebruik een UPDATE-statement met een WHERE-clause op basis van het id van de student.

</details>

<details>
<summary>Hint 2</summary>

Net als bij INSERT gebruik je `executeUpdate()` — die methode werkt voor alle statements die data wijzigen.

</details>

<details>
<summary>Hint 3</summary>

```java
int rows = stmt.executeUpdate("UPDATE students SET age = 25 WHERE name = 'Jan'");
System.out.println("Updated rows: " + rows);
```

Haal daarna alle studenten opnieuw op met je SELECT-code om het resultaat te verifiëren.

</details>

---

## Part 5: Student verwijderen

### What you will do

Verwijder een student uit de database. Controleer het resultaat door het aantal affected rows te printen en de resterende studenten op te halen.

### Success criteria

- `executeUpdate()` retourneert `1`
- Na het ophalen van alle studenten is de verwijderde student niet meer zichtbaar
- Er zijn nog precies 2 studenten over

### Hints

<details>
<summary>Hint 1</summary>

Een DELETE-statement werkt vergelijkbaar met UPDATE — je hebt een WHERE-clause obv id nodig om de juiste student te targeten.

</details>

<details>
<summary>Hint 2</summary>

Vergeet niet om na het verwijderen opnieuw alle studenten op te halen, zodat je kunt verifiëren dat het gelukt is.

</details>

<details>
<summary>Hint 3</summary>

```java
int rows = stmt.executeUpdate("DELETE FROM students WHERE id = 1'");
System.out.println("Deleted rows: " + rows);
// Haal daarna alle studenten opnieuw op
```

</details>

---

# Bonus Challenge (Optional)

Voeg een `COUNT(*)`-query toe die je na elke CRUD-operatie aanroept om het totale aantal studenten in de tabel te printen. Zo heb je na elke stap een extra controle naast de affected rows. Kun je dit in een aparte methode zetten die je hergebruikt?

---

# Reflection Questions

### Implementation & Trade-offs

1. Je gebruikt nu `Statement` voor alle queries. Welk risico brengt dit met zich mee als de input van een gebruiker zou komen in plaats van hardcoded strings?
2. Wat zijn de voor- en nadelen van `CREATE TABLE IF NOT EXISTS` ten opzichte van eerst checken of de tabel al bestaat?

### Production Readiness

3. Je JDBC URL wijst nu naar een lokaal bestand (`jdbc:h2:file:...`). Wat zou er veranderen als je dit in een productieomgeving zou deployen?
4. Welke informatie zou je willen loggen bij elke CRUD-operatie als dit een productiesysteem was?

### Debugging & Problem Solving

5. Stel dat je `executeUpdate()` `0` retourneert bij een UPDATE. Wat zou de oorzaak kunnen zijn, en hoe debug je dat?
6. Wat gebeurt er als je de database-file halverwege een run verwijdert? Hoe zou je dit als developer kunnen opvangen?

### Adaptation / Transfer

7. Als je dit systeem zou uitbreiden met een tweede tabel `courses` die een relatie heeft met `students`, welke extra JDBC-code zou je dan nodig hebben?
8. Hoe zou je deze code herstructureren als je dezelfde CRUD-operaties wilt aanbieden via een REST API in plaats van een console-applicatie?

---

