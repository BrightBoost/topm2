# Lab: Transactieprobleem zonder transactie

## Scenario / Context

Je werkt als developer aan het inschrijfportaal van een onderwijsinstelling. Studenten kunnen zich via de applicatie inschrijven voor cursussen. Elke cursus heeft een beperkt aantal plekken — het veld `available_spots` in de `courses`-tabel houdt bij hoeveel plaatsen er nog vrij zijn. Inschrijven betekent twee dingen tegelijk: een rij toevoegen aan de `enrollments`-tabel én het aantal beschikbare plekken in `courses` met 1 verlagen. Een collega heeft deze logica geïmplementeerd, en het werkt prima — totdat een student zich probeert in te schrijven voor een cursus die al vol zit. De UPDATE op `courses` faalt (er is een CHECK-constraint die voorkomt dat `available_spots` onder 0 komt), maar de INSERT in `enrollments` is al uitgevoerd en staat in de database. Resultaat: een spookregistratie. De student staat ingeschreven voor een cursus waar geen plek meer is, en het systeem denkt dat alles in orde is. Jouw opdracht: reproduceer dit probleem en ervaar waarom het gevaarlijk is.

---

## Learning Goals

- Een INSERT en een UPDATE die logisch bij elkaar horen uitvoeren met JDBC en observeren wat er in de database terechtkomt als één van de twee faalt
- Een constraint violation veroorzaken op een UPDATE en het effect op een eerder uitgevoerde INSERT analyseren
- Beoordelen waarom auto-commit mode kan leiden tot inconsistente data wanneer meerdere tabellen betrokken zijn bij één bedrijfsoperatie
- Beredeneren welk mechanisme nodig is om dit probleem te voorkomen (voorbereiding op transacties)

---

## Prerequisites

- Java 21 geïnstalleerd
- Maven geïnstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Basiskennis JDBC (Connection, Statement, PreparedStatement, ResultSet)
- Dag 1 mini-exercises afgerond (CRUD-operaties, connecties met H2)

---

# Lab Parts

Dit lab bevat **3 delen**.

---

## Part 1: Database opzetten met twee tabellen

### What you will do

Maak een nieuw Java-project (of hergebruik je dag 1 project) met een H2-database. Maak twee tabellen aan: `courses` (met een CHECK-constraint dat `available_spots >= 0`) en `enrollments` (met een FOREIGN KEY naar `courses`). Vul de `courses`-tabel met één cursus die nog maar **0** beschikbare plekken heeft — de cursus zit dus vol.

### Success criteria

- De applicatie maakt verbinding met een H2 file-based database
- De `courses`-tabel heeft kolommen `id` (auto-increment), `name` en `available_spots` (met CHECK >= 0)
- De `enrollments`-tabel heeft kolommen `id` (auto-increment), `student_name`, `email` en `course_id` (FOREIGN KEY naar `courses`)
- Er staat één cursus in de database: "Advanced JDBC" met `available_spots = 0`
- De `enrollments`-tabel is leeg

### Hints

<details>
<summary>Hint 1</summary>

Je kunt je bestaande project uit dag 1 hergebruiken. Je hebt de H2 dependency al in je `pom.xml` staan.

</details>

<details>
<summary>Hint 2</summary>

Gebruik `DROP TABLE IF EXISTS` (let op de volgorde vanwege foreign keys!) gevolgd door `CREATE TABLE` om elke keer met schone tabellen te beginnen. Maak eerst `enrollments` weg, dan `courses`.

</details>

<details>
<summary>Hint 3</summary>

Gebruik een CHECK-constraint op `available_spots` om te garanderen dat het aantal niet onder 0 kan komen. Zo wordt een UPDATE die het veld naar -1 zou brengen automatisch geweigerd door de database.

</details>

<details>
<summary>Hint 4</summary>

```sql
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS courses;

CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    available_spots INT CHECK (available_spots >= 0)
);

CREATE TABLE enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(255),
    email VARCHAR(255),
    course_id BIGINT,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

INSERT INTO courses (name, available_spots) VALUES ('Advanced JDBC', 0);
```

</details>

---

## Part 2: Inschrijving simuleren — INSERT slaagt, UPDATE faalt

### What you will do

Schrijf code die een inschrijving simuleert. Stap 1: voeg een rij toe aan `enrollments` (de student "schrijft zich in"). Stap 2: verlaag `available_spots` in `courses` met 1. Omdat de cursus al vol zit (0 plekken), zal de UPDATE falen op de CHECK-constraint. Vang de exception op en ga door met het programma.

### Success criteria

- De INSERT in `enrollments` slaagt (1 affected row)
- De UPDATE op `courses` gooit een `SQLException` vanwege de CHECK-constraint (0 - 1 = -1, dat mag niet)
- De exception wordt netjes opgevangen — het programma crasht niet

### Hints

<details>
<summary>Hint 1</summary>

De connectie staat standaard in auto-commit mode. Dat betekent dat de INSERT in `enrollments` direct gecommit wordt, nog voordat de UPDATE op `courses` wordt uitgevoerd.

</details>

<details>
<summary>Hint 2</summary>

Doe de INSERT eerst, en de UPDATE daarna. Gebruik twee aparte `PreparedStatement`-objecten op dezelfde connectie. Wrap de UPDATE in een try-catch om de `SQLException` op te vangen.

</details>

<details>
<summary>Hint 3</summary>

```java
try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
    // Stap 1: student inschrijven
    try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO enrollments (student_name, email, course_id) VALUES (?, ?, ?)")) {
        ps.setString(1, "Jan");
        ps.setString(2, "jan@university.nl");
        ps.setLong(3, 1); // course_id van 'Advanced JDBC'
        System.out.println("INSERT enrollments affected rows: " + ps.executeUpdate());
    }

    // Stap 2: beschikbare plekken verlagen
    try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE courses SET available_spots = available_spots - 1 WHERE id = ?")) {
        ps.setLong(1, 1);
        System.out.println("UPDATE courses affected rows: " + ps.executeUpdate());
    }
}
```

Wrap het tweede blok in een extra try-catch om de exception op te vangen als de CHECK-constraint het niet toelaat.

</details>

---

## Part 3: De schade inspecteren

### What you will do

Voer SELECT-queries uit op beide tabellen om te zien wat de staat van de database is na de gefaalde operatie. Beantwoord de vraag: is de database in een consistente staat?

### Success criteria

- De SELECT op `enrollments` toont dat Jan wél is ingeschreven
- De SELECT op `courses` toont dat `available_spots` nog steeds 0 is (de UPDATE is niet uitgevoerd)
- Je kunt uitleggen waarom dit een probleem is: er is een inschrijving zonder dat er een plek is afgetrokken
- Je kunt benoemen welk mechanisme dit zou kunnen voorkomen

### Hints

<details>
<summary>Hint 1</summary>

Voer twee queries uit: `SELECT * FROM enrollments` en `SELECT * FROM courses`. Vergelijk de resultaten met wat je zou verwachten als de inschrijving correct was verlopen.

</details>

<details>
<summary>Hint 2</summary>

Jan staat in de `enrollments`-tabel, maar de cursus "Advanced JDBC" heeft nog steeds 0 beschikbare plekken. De database vertelt twee tegenstrijdige verhalen: Jan is ingeschreven, maar er was geen plek. Dit is een klassiek voorbeeld van een inconsistente staat.

</details>

<details>
<summary>Hint 3</summary>

Het probleem is auto-commit: de INSERT wordt meteen definitief gemaakt, onafhankelijk van of de UPDATE daarna slaagt. Als je de twee operaties als één geheel wilt behandelen, heb je een manier nodig om te zeggen: "commit pas als alles gelukt is, en draai alles terug als er iets misgaat." Dat is precies wat een transactie doet.

</details>

<details>
<summary>Hint 4</summary>

Na je SELECT-queries, print de resultaten en voeg een comment toe in je code:

```java
// TODO: Jan staat ingeschreven maar er is geen plek afgetrokken.
// Hoe zorgen we ervoor dat INSERT + UPDATE samen slagen of samen falen?
// Antwoord: door een transactie te gebruiken (auto-commit uitzetten)
```

</details>

---

# Bonus Challenge (Optioneel)

Stel je voor dat de cursus nog wél 1 plek had (`available_spots = 1`). Simuleer nu twee studenten die zich "tegelijkertijd" inschrijven — voer de enrollment-logica twee keer achter elkaar uit zonder de database tussendoor te resetten. De eerste inschrijving slaagt volledig (INSERT + UPDATE), maar bij de tweede faalt de UPDATE weer. Nu heb je twee inschrijvingen in `enrollments`, maar `available_spots` is slechts met 1 verlaagd (van 1 naar 0). De administratie denkt dat er nog capaciteitsproblemen zijn terwijl er eigenlijk een spookinschrijving in het systeem zit. Bedenk hoe dit in een productieomgeving met honderden gelijktijdige inschrijvingen uit de hand kan lopen.

---

# Reflectievragen

### Implementatie & Afwegingen

- In auto-commit mode wordt elke `executeUpdate()` direct gecommit. Waarom is dit handig voor losse, onafhankelijke operaties, maar problematisch zodra een bedrijfsactie (zoals een inschrijving) meerdere tabellen raakt?
- Wat zou er veranderen als je de CHECK-constraint op `available_spots` niet had? Zou het probleem dan verdwijnen, of zou het alleen moeilijker te detecteren worden?

### Production Readiness

- Stel je voor dat dit inschrijfsysteem in productie draait tijdens een populaire inschrijfperiode met honderden studenten tegelijk. Wat zijn de gevolgen als inschrijvingen halverwege falen zonder transactiemanagement?
- Hoe zou je achteraf kunnen detecteren dat er spookinschrijvingen in het systeem zitten — enrollments zonder bijbehorende verlaging van `available_spots`?

### Debugging & Problem Solving

- Als je de `SQLException` niet opvangt, crasht het programma. Maar als je hem wél opvangt en gewoon doorgaat, merk je misschien niet dat de database inconsistent is. Welke aanpak is veiliger?
- De inconsistentie zit verspreid over twee tabellen (`enrollments` en `courses`). Waarom maakt dat het debuggen lastiger dan wanneer het probleem in één tabel zit?

### Aanpassing / Transfer

- Dit voorbeeld combineert een INSERT en een UPDATE. Bedenk een ander scenario waar een INSERT in de ene tabel en een DELETE in de andere tabel samen moeten slagen (bijvoorbeeld: een student wisselt van cursus). Welke risico's zie je daar?
- Hoe zou dit probleem zich manifesteren als niet alleen de database maar ook een extern systeem (bijvoorbeeld een e-mailservice die een bevestiging stuurt) betrokken is bij de inschrijving?
