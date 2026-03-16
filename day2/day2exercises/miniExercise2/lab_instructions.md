# Lab: Transacties in JDBC

## Scenario / Context

In de vorige mini-exercise zag je het probleem: een INSERT in `enrollments` zonder bijbehorende UPDATE op `courses`, en je database is inconsistent — een spookregistratie. Je teamlead heeft je gevraagd om dit op te lossen. Het inschrijfproces moet atomair zijn: of de enrollment wordt aangemaakt én de beschikbare plekken worden verlaagd, of er wijzigt helemaal niets. Je gaat nu de code aanpassen zodat de INSERT en UPDATE in een transactie draaien. Eerst het happy path (cursus heeft plekken, alles slaagt), dan het unhappy path (cursus is vol, alles wordt teruggedraaid). Als dat werkt, wil je teamlead ook een "best effort" variant: een student schrijft zich in voor twee cursussen tegelijk — als de inschrijving voor de ene cursus faalt, moet de inschrijving voor de andere cursus alsnog behouden blijven. Daar komen savepoints om de hoek kijken.

---

## Learning Goals

- `setAutoCommit(false)` toepassen om handmatig transactiebeheer over te nemen van de JDBC-connectie
- Het try-catch-finally patroon implementeren met `commit()` en `rollback()` voor correcte transactieafhandeling bij operaties die meerdere tabellen raken
- Verifiëren dat een rollback daadwerkelijk alle operaties binnen de transactie ongedaan maakt, inclusief de INSERT die wel slaagde
- Een savepoint inzetten om gedeeltelijk terug te draaien binnen een transactie en beredeneren wanneer dit nuttig is
- Evalueren hoe transactiemanagement het spookregistratieprobleem uit mini-exercise 1 oplost

---

## Prerequisites

- Java 21 geïnstalleerd
- Maven geïnstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Basiskennis JDBC (Connection, PreparedStatement, ResultSet)
- Mini-exercise 1 afgerond (je hebt het probleem zonder transacties gezien)
- H2-project uit mini-exercise 1 (of een nieuw Maven-project met H2 dependency)

---

# Lab Parts

Dit lab bevat **4 delen**.

---

## Part 1: Happy path — enrollment met commit

### What you will do

Zet de database op met dezelfde twee tabellen als mini-exercise 1 (`courses` en `enrollments`), maar geef de cursus nu **1** beschikbare plek. Zet auto-commit uit op je connectie, voer de INSERT in `enrollments` uit, verlaag `available_spots` met 1, en commit de transactie. Controleer daarna dat de enrollment bestaat en de cursus 0 plekken over heeft.

### Success criteria

- De `courses`-tabel bevat "Advanced JDBC" met `available_spots = 1`
- Auto-commit staat uit voordat de operaties worden uitgevoerd
- De INSERT in `enrollments` slaagt
- De UPDATE op `courses` slaagt (`available_spots` gaat van 1 naar 0)
- Na `commit()` staat de enrollment in de database en heeft de cursus 0 plekken over
- Wat moet je daarna ook alweer doen?

### Hints

<details>
<summary>Hint 1</summary>

Denk aan het patroon: eerst `setAutoCommit(false)`, dan je operaties, dan `commit()`. Zonder die eerste stap ben je nog steeds in auto-commit mode.

</details>

<details>
<summary>Hint 2</summary>

Gebruik dezelfde tabelstructuur als mini-exercise 1 (`courses` met CHECK-constraint en `enrollments` met foreign key). Het enige verschil: `available_spots` begint op 1 in plaats van 0.

</details>

<details>
<summary>Hint 3</summary>

Implementeer het try-catch-finally patroon uit de slides:

```java
conn.setAutoCommit(false);
try {
    // INSERT into enrollments
    // UPDATE courses SET available_spots = available_spots - 1
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
} finally {
    conn.setAutoCommit(true);
}
```

</details>

<details>
<summary>Hint 4</summary>

Voeg na het finally-blok twee SELECT-queries uit om te verifiëren:

```java
try (Statement stmt = conn.createStatement()) {
    ResultSet rs = stmt.executeQuery("SELECT * FROM enrollments");
    while (rs.next()) {
        System.out.println("Enrollment: " + rs.getString("student_name")
            + " | " + rs.getString("email"));
    }
    rs = stmt.executeQuery("SELECT * FROM courses");
    while (rs.next()) {
        System.out.println("Course: " + rs.getString("name")
            + " | spots: " + rs.getInt("available_spots"));
    }
}
```

</details>

---

## Part 2: Unhappy path — cursus is vol, rollback

### What you will do

Herhaal het scenario uit mini-exercise 1: de cursus heeft **0** beschikbare plekken. Voer de INSERT in `enrollments` uit, dan de UPDATE op `courses`. De UPDATE faalt op de CHECK-constraint. Maar nu heb je transactiemanagement: vang de exception op en voer een rollback uit. Controleer dat de enrollment ook weg is.

### Success criteria

- De `courses`-tabel bevat "Advanced JDBC" met `available_spots = 0`
- Auto-commit staat uit
- De INSERT in `enrollments` slaagt binnen de transactie
- De UPDATE op `courses` faalt (CHECK-constraint: `available_spots` kan niet onder 0)
- `rollback()` wordt aangeroepen in het catch-blok
- Een SELECT op `enrollments` bevestigt dat de tabel leeg is — de spookregistratie is voorkomen

### Hints

<details>
<summary>Hint 1</summary>

Dit is het grote verschil met mini-exercise 1: omdat auto-commit uit staat, is de INSERT in `enrollments` nog niet gecommit wanneer de UPDATE faalt. Een rollback maakt alles ongedaan.

</details>

<details>
<summary>Hint 2</summary>

Gebruik dezelfde code als part 1, maar begin met `available_spots = 0`. Het verschil in uitkomst is puur het gevolg van de CHECK-constraint die de UPDATE blokkeert.

</details>

<details>
<summary>Hint 3</summary>

Zorg dat de INSERT en de UPDATE binnen hetzelfde try-blok staan, zodat een falen van de UPDATE automatisch naar het catch-blok springt:

```java
conn.setAutoCommit(false);
try {
    // INSERT into enrollments (slaagt)
    // UPDATE courses SET available_spots = available_spots - 1 (faalt!)
    conn.commit();
} catch (SQLException e) {
    System.out.println("Fout: " + e.getMessage());
    conn.rollback();
    System.out.println("Rollback uitgevoerd — geen spookregistratie!");
} finally {
    conn.setAutoCommit(true);
}
```

</details>

<details>
<summary>Hint 4</summary>

Na de transactie, voer een SELECT uit op `enrollments`. Je zou 0 rijen moeten zien. Vergelijk dit met hoe het ging in mini-exercise 1, waar Jan nog wel in de `enrollments`-tabel stond terwijl er geen plek was afgetrokken. Dat is het verschil dat de transactie maakt.

</details>

---

## Part 3: Verifiëren en vergelijken

### What you will do

Draai part 1 (happy path) en part 2 (unhappy path) achter elkaar in hetzelfde programma. Begin elke keer met schone tabellen. Print na elke transactie de inhoud van beide tabellen en vergelijk de resultaten.

### Success criteria

- Na het happy path: 1 enrollment in de database, cursus heeft 0 plekken over — consistent
- Na het unhappy path: 0 enrollments in de database, cursus heeft nog steeds 0 plekken — ook consistent
- De console-output maakt het verschil duidelijk zichtbaar
- Je kunt uitleggen hoe dit het spookregistratieprobleem uit mini-exercise 1 oplost

### Hints

<details>
<summary>Hint 1</summary>

Gebruik `DROP TABLE IF EXISTS` (eerst `enrollments`, dan `courses`) gevolgd door `CREATE TABLE` en de initiële `INSERT INTO courses` aan het begin van elke test om met schone tabellen te starten.

</details>

<details>
<summary>Hint 2</summary>

Maak twee helper-methoden: `printEnrollments(conn)` en `printCourses(conn)`. Roep beide aan na elke transactie zodat je duidelijk ziet wat de staat is van de hele database.

</details>

<details>
<summary>Hint 3</summary>

Structureer je main-methode zo:

```java
System.out.println("=== HAPPY PATH (1 plek beschikbaar) ===");
setupTables(conn, 1); // available_spots = 1
enrollStudent(conn, "Jan", "jan@university.nl", 1);
printEnrollments(conn);
printCourses(conn);

System.out.println("=== UNHAPPY PATH (0 plekken beschikbaar) ===");
setupTables(conn, 0); // available_spots = 0
enrollStudent(conn, "Jan", "jan@university.nl", 1);
printEnrollments(conn);
printCourses(conn);
```

</details>

---

## Part 4: Savepoint — gedeeltelijke rollback

### What you will do

Implementeer een transactie waarin een student zich inschrijft voor twee cursussen tegelijk. Maak twee cursussen aan: "Advanced JDBC" (1 plek) en "Database Design" (0 plekken, vol). Doe de enrollment + UPDATE voor de eerste cursus, maak een savepoint, en probeer dan de enrollment + UPDATE voor de tweede cursus. Die faalt. Draai terug naar het savepoint en commit. Verifieer dat de inschrijving voor "Advanced JDBC" behouden is, maar die voor "Database Design" niet.

### Success criteria

- Twee cursussen bestaan: "Advanced JDBC" (1 plek) en "Database Design" (0 plekken)
- Een savepoint wordt aangemaakt na de eerste succesvolle enrollment
- De enrollment voor de tweede cursus faalt (CHECK-constraint)
- `rollback(savepoint)` draait alleen de tweede enrollment terug
- Na commit staat de enrollment voor "Advanced JDBC" in de database en is `available_spots` verlaagd
- Er is geen enrollment voor "Database Design" en die cursus heeft nog steeds 0 plekken

### Hints

<details>
<summary>Hint 1</summary>

Een savepoint is als een bladwijzer in je transactie. Als je terugdraait naar een savepoint, blijft alles van voor dat punt intact — in dit geval de succesvolle inschrijving voor de eerste cursus.

</details>

<details>
<summary>Hint 2</summary>

Je hebt een geneste try-catch nodig: de buitenste voor de hele transactie, de binnenste rondom de riskante enrollment voor de volle cursus.

</details>

<details>
<summary>Hint 3</summary>

Gebruik `connection.setSavepoint("afterFirstEnrollment")` en later `connection.rollback(savepoint)` in de binnenste catch:

```java
conn.setAutoCommit(false);
try {
    // Enrollment cursus 1: INSERT enrollments + UPDATE courses (slaagt)
    Savepoint sp = conn.setSavepoint("afterFirstEnrollment");

    try {
        // Enrollment cursus 2: INSERT enrollments + UPDATE courses (faalt!)
    } catch (SQLException e) {
        System.out.println("Cursus 2 vol, rollback naar savepoint");
        conn.rollback(sp);
    }

    conn.commit();
} catch (SQLException e) {
    conn.rollback();
} finally {
    conn.setAutoCommit(true);
}
```

</details>

<details>
<summary>Hint 4</summary>

Na de commit, print de inhoud van beide tabellen. Je zou één enrollment moeten zien (voor "Advanced JDBC"), en de `available_spots` voor die cursus zou 0 moeten zijn. "Database Design" zou ongewijzigd moeten zijn. Dit is het "best effort" patroon: wat kan, wordt bewaard. Wat faalt, wordt overgeslagen.

</details>

---

# Bonus Challenge (Optioneel)

Refactor je code zodat de transactielogica herbruikbaar is. Maak een methode `executeInTransaction(Connection conn, TransactionalOperation operation)` die het try-catch-finally patroon afhandelt. De `TransactionalOperation` is een functioneel interface met een methode `void execute(Connection conn) throws SQLException`. Gebruik deze methode om zowel het happy path als het unhappy path uit te voeren. Dit is een stap richting hoe frameworks als Spring transacties beheren — je scheidt de transactie-infrastructuur van de business logica.

---

# Reflectievragen

### Implementatie & Afwegingen

- Je hebt `setAutoCommit(true)` in het finally-blok gezet. Wat zou er gebeuren als je dat vergeet en de connectie hergebruikt voor een volgende operatie — bijvoorbeeld een losse SELECT die ineens ook onderdeel van een ongecommitte transactie wordt?
- Bij het savepoint-patroon kies je ervoor om door te gaan als één van de twee inschrijvingen faalt. In welke situaties is dit wenselijk (denk aan een "wensenlijst" van cursussen), en wanneer zou je liever de hele transactie afbreken (denk aan een pakketinschrijving waar alle cursussen verplicht zijn)?

### Production Readiness

- In productie deel je connecties via een connection pool. Wat gebeurt er als je een connectie teruggeeft aan de pool terwijl `autoCommit` nog op `false` staat en er ongecommitte wijzigingen zijn?
- Hoe zou je in een productiesysteem loggen welke enrollments succesvol waren en welke zijn teruggedraaid, zodat de studentenadministratie weet wat er is gebeurd?

### Debugging & Problem Solving

- Als je rollback niet het verwachte effect heeft (de enrollment staat er toch in), wat zijn de eerste dingen die je controleert? Denk aan auto-commit status, scope van het try-blok, en of de rollback daadwerkelijk wordt bereikt.
- De inconsistentie in mini-exercise 1 zat verspreid over twee tabellen (`enrollments` en `courses`). Hoe helpt het om te denken in "één transactie = één bedrijfsoperatie" bij het voorkomen van dit soort cross-table inconsistenties?

### Aanpassing / Transfer

- In dit lab gebruikte je `setAutoCommit(false)` direct op de connectie. Hoe denk je dat een framework als Spring dit anders aanpakt, en waarom zou dat handiger zijn in een grotere applicatie met veel methodes die transacties nodig hebben?
- Bedenk een scenario buiten onderwijs waar het savepoint-patroon nuttig zou zijn. Denk bijvoorbeeld aan een e-commerce systeem waar een bestelling uit meerdere items bestaat — sommige op voorraad, sommige niet. Beschrijf het gewenste gedrag.
