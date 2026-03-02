# Lab: Student DAO met JDBC

## Scenario

De onderwijsinstelling waar je werkt is onder de indruk van je eerste CRUD-experimenten, en al dat gegoochel met SQL injection heeft nog meer vertrouwen gewekt. Nu is het tijd om een echte, gestructureerde data-laag te bouwen. Geen losse SQL-statements meer verspreid door je `main`-methode, maar een nette scheiding van verantwoordelijkheden. Je gaat een Data Access Object (DAO) pattern implementeren voor studentgegevens. Dit is precies hoe je in een professioneel project je database-code organiseert: een interface dat beschrijft _wat_ je kunt doen, en een implementatie die bepaalt _hoe_ het gebeurt.

---

## Learning Goals

- Een DAO-interface definiëren met CRUD-methodes en deze implementeren met JDBC en PreparedStatements
- ResultSets mappen naar Java-objecten en deze correct verwerken in een lijst of Optional
- Het DAO-pattern toepassen om database-logica te scheiden van applicatielogica en beoordelen waarom die scheiding waardevol is

---

## Prerequisites

- Java 21 geïnstalleerd
- Maven geïnstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Mini Exercise 1 en 2 afgerond (basiskennis JDBC, Statement, PreparedStatement, ResultSet)
- Basiskennis SQL (CREATE TABLE, INSERT, SELECT, UPDATE, DELETE)

---

# Lab Parts

Dit lab bevat **5 delen**, plus een bonusopdracht.

---

## Part 1: De Student class maken

### What you will do

Maak de `Student` class (of record) compleet in het startersproject. Deze class representeert een rij uit de `students`-tabel en heeft velden voor id, name, email en age.

Open het project in de map `student-dao-exercise` en bekijk het bestand `Student.java`. Vul de TODOs in.

### Success criteria

- De `Student` class compileert zonder fouten
- Je kunt een `Student`-object aanmaken met alle velden
- `toString()` geeft een leesbare weergave (bijv. `Student{id=1, name='Jan', email='jan@mail.nl', age=22}`)

### Hints

<details>
<summary>Hint 1</summary>

Bedenk welke velden je nodig hebt en welk type bij elk veld past. Een id is typisch een `long`, leeftijd een `int`, en name/email zijn Strings.

</details>

<details>
<summary>Hint 2</summary>

Je kunt kiezen tussen een gewone class met private velden + getters/setters, of een Java `record`. Een record is compacter maar immutable. Voor deze oefening werken beide.

</details>

<details>
<summary>Hint 3</summary>

Als je een class kiest: maak minstens twee constructors. Eentje met alle velden (voor als je uit de database leest), en eentje zonder id (voor nieuwe studenten die nog geen id hebben).

</details>

<details>
<summary>Hint 4</summary>

```java
public class Student {
    private long id;
    private String name;
    private String email;
    private int age;

    public Student(long id, String name, String email, int age) { ... }
    public Student(String name, String email, int age) { ... }

    // getters, setters, toString
}
```

Of als record:

```java
public record Student(long id, String name, String email, int age) {}
```

Let op: bij een record heb je geen constructor zonder id tenzij je een extra static factory method toevoegt.

</details>

---

## Part 2: Het DAO-interface begrijpen en de implementatie starten

### What you will do

Bekijk het `StudentDao` interface dat al voor je klaarstaat. Implementeer vervolgens in `JdbcStudentDao` de `createTable()` methode en de `save()` methode. Test ze vanuit `StudentDaoApp.main()`.

### Success criteria

- De `students`-tabel wordt aangemaakt bij het starten van de applicatie
- Je kunt studenten opslaan via `dao.save()`
- In de console verschijnt een bevestiging per opgeslagen student
- De applicatie kan meerdere keren gestart worden zonder fouten

### Hints

<details>
<summary>Hint 1</summary>

Gebruik `CREATE TABLE IF NOT EXISTS` met een `IDENTITY` of `AUTO_INCREMENT` kolom voor de id. Bij H2 werkt `BIGINT AUTO_INCREMENT` prima als primary key.

</details>

<details>
<summary>Hint 2</summary>

Voor `save()` heb je een INSERT-statement nodig met placeholders (`?`). Gebruik `PreparedStatement` en stel de parameters in met `setString()` en `setInt()`.

</details>

<details>
<summary>Hint 3</summary>

Vergeet niet je `Connection` en `PreparedStatement` te sluiten. De makkelijkste manier is try-with-resources:

```java
try (Connection conn = getConnection();
     PreparedStatement ps = conn.prepareStatement("INSERT INTO ...")) {
    // parameters zetten en uitvoeren
}
```

</details>

<details>
<summary>Hint 4</summary>

```java
public void createTable() {
    String sql = "CREATE TABLE IF NOT EXISTS students ("
        + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
        + "name VARCHAR(255), "
        + "email VARCHAR(255), "
        + "age INT)";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.execute();
    } catch (SQLException e) {
        throw new RuntimeException("Fout bij aanmaken tabel", e);
    }
}
```

</details>

---

## Part 3: findAll() en findById() implementeren

### What you will do

Implementeer de twee lees-methodes van de DAO: `findAll()` die alle studenten teruggeeft, en `findById()` die een enkele student zoekt op basis van ID.

### Success criteria

- `findAll()` retourneert een lijst met alle eerder opgeslagen studenten
- `findById()` met een bestaand ID retourneert de juiste student
- `findById()` met een niet-bestaand ID retourneert `Optional.empty()`
- De studenten worden correct geprint in de console

### Hints

<details>
<summary>Hint 1</summary>

Bij `findAll()` loop je door de `ResultSet` met `while (rs.next())`. Bij `findById()` gebruik je `if (rs.next())` omdat je maximaal een resultaat verwacht.

</details>

<details>
<summary>Hint 2</summary>

Gebruik `rs.getLong("id")`, `rs.getString("name")`, `rs.getString("email")` en `rs.getInt("age")` om kolommen uit te lezen.

</details>

<details>
<summary>Hint 3</summary>

Maak een kleine helper-methode die een `ResultSet`-rij mapt naar een `Student`-object. Dat voorkomt dubbele code in `findAll()` en `findById()`.

</details>

<details>
<summary>Hint 4</summary>

```java
@Override
public List<Student> findAll() {
    List<Student> students = new ArrayList<>();
    String sql = "SELECT id, name, email, age FROM students";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            students.add(mapRow(rs));
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    return students;
}

private Student mapRow(ResultSet rs) throws SQLException {
    return new Student(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getInt("age")
    );
}
```

</details>

---

## Part 4: update() en delete() implementeren

### What you will do

Implementeer de laatste twee CRUD-methodes: `update()` om studentgegevens te wijzigen, en `delete()` om een student te verwijderen. Test alles vanuit je `main`-methode.

### Success criteria

- Na `update()` toont `findById()` de bijgewerkte gegevens
- Na `delete()` retourneert `findById()` een `Optional.empty()`
- Het aantal affected rows wordt geprint in de console (verwacht: 1)
- `findAll()` toont na de wijzigingen de juiste inhoud

### Hints

<details>
<summary>Hint 1</summary>

Een UPDATE-statement heeft een WHERE-clause nodig om de juiste rij te targeten. Gebruik het id van de Student.

</details>

<details>
<summary>Hint 2</summary>

Gebruik `executeUpdate()` in plaats van `execute()` — die geeft het aantal affected rows terug als `int`.

</details>

<details>
<summary>Hint 3</summary>

Let op de volgorde van je parameters in het PreparedStatement. Als je SQL `SET name=?, email=?, age=? WHERE id=?` is, dan is id de vierde parameter.

</details>

<details>
<summary>Hint 4</summary>

```java
@Override
public void update(Student student) {
    String sql = "UPDATE students SET name=?, email=?, age=? WHERE id=?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, student.getName());
        ps.setString(2, student.getEmail());
        ps.setInt(3, student.getAge());
        ps.setLong(4, student.getId());
        int rows = ps.executeUpdate();
        System.out.println("Updated rows: " + rows);
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
```

</details>

---

## Part 5: Alles testen vanuit main

### What you will do

Schrijf een complete testsequentie in `StudentDaoApp.main()` die alle DAO-methodes oefent: aanmaken, opslaan, ophalen, bijwerken en verwijderen. Verifieer na elke stap of het resultaat klopt.

### Success criteria

- De console-output laat de volledige levenscyclus zien: toevoegen, lezen, updaten, verwijderen
- Na het toevoegen van 3 studenten toont `findAll()` precies 3 resultaten
- Na een update is het gewijzigde veld zichtbaar in de output
- Na een delete is de student verdwenen uit `findAll()`

### Hints

<details>
<summary>Hint 1</summary>

Werk stap voor stap: eerst `save()` aanroepen, dan `findAll()` om te controleren, vervolgens `findById()`, dan `update()`, weer `findById()` om de wijziging te checken, en tenslotte `delete()` gevolgd door `findAll()`.

</details>

<details>
<summary>Hint 2</summary>

Gebruik `System.out.println("--- Alle studenten ---")` als kopjes tussen secties, zodat de output leesbaar is.

</details>

<details>
<summary>Hint 3</summary>

Maak studenten aan zonder id (laat de database het id genereren):

```java
dao.save(new Student("Alice de Vries", "alice@university.nl", 21));
dao.save(new Student("Bob Jansen", "bob@university.nl", 23));
dao.save(new Student("Charlie Bakker", "charlie@university.nl", 20));
```

</details>

<details>
<summary>Hint 4</summary>

Een complete testsequentie:

```java
// Save
dao.save(new Student("Alice", "alice@uni.nl", 21));
// ...meer studenten

// FindAll
System.out.println("--- Alle studenten ---");
dao.findAll().forEach(System.out::println);

// FindById
System.out.println("--- Zoek op ID 1 ---");
dao.findById(1).ifPresentOrElse(
    s -> System.out.println("Gevonden: " + s),
    () -> System.out.println("Niet gevonden")
);

// Update
Student toUpdate = new Student(1, "Alice Updated", "alice.new@uni.nl", 22);
dao.update(toUpdate);

// Delete
dao.delete(2);

// Verify
System.out.println("--- Na wijzigingen ---");
dao.findAll().forEach(System.out::println);
```

</details>

---

# Bonus Challenge (Optional)

### Bonus A: Tweede entiteit — Course DAO

Maak een `Course` class met velden `id`, `title`, `credits` en `teacherName`. Maak een `CourseDao` interface met dezelfde CRUD-methodes als `StudentDao` en implementeer het met JDBC. Kijk kritisch naar je code: welke delen zijn bijna identiek aan `JdbcStudentDao`? Zou je een generieke basis-class of utility kunnen maken om duplicatie te voorkomen?

### Bonus B: Interactieve console-applicatie

Voeg een interactieve loop toe aan `StudentDaoApp` met een `Scanner`. Geef de gebruiker een menu:

```
1. Alle studenten tonen
2. Student zoeken op ID
3. Nieuwe student toevoegen
4. Student bijwerken
5. Student verwijderen
0. Afsluiten
```

Laat de gebruiker gegevens invoeren via de console en voer de bijbehorende DAO-operatie uit. Als je ook de Course DAO hebt gemaakt, voeg dan ook cursusopties toe aan het menu.

---

# Reflection Questions

### Implementation & Trade-offs

1. Je hebt een interface (`StudentDao`) gemaakt en een JDBC-implementatie. Wat zou je moeten veranderen als je morgen overschakelt van H2 naar PostgreSQL? En wat hoeft er juist _niet_ te veranderen dankzij het interface?

2. Elke DAO-methode opent en sluit een eigen `Connection`. Wat zijn de voor- en nadelen van deze aanpak ten opzichte van het delen van een enkele connectie over meerdere methodes?

### Production Readiness

3. In de huidige implementatie gooi je een `RuntimeException` bij SQL-fouten. Hoe zou je foutafhandeling inrichten in een productieomgeving? Denk aan logging, specifieke exceptions en het informeren van de gebruiker.

4. Wat gebeurt er als twee gebruikers tegelijkertijd dezelfde student proberen te updaten? Welke problemen kunnen ontstaan en hoe zou je die kunnen voorkomen?

### Debugging & Problem Solving

5. Stel dat `findAll()` een lege lijst retourneert terwijl je net studenten hebt opgeslagen. Noem drie mogelijke oorzaken en hoe je elk zou diagnosticeren.

6. Je krijgt een `SQLException` met de melding "Table STUDENTS not found". De `createTable()`-methode is wel aangeroepen. Wat kan er mis zijn?

### Adaptation / Transfer

7. Als je de `CourseDao` hebt geïmplementeerd: hoeveel van de code was bijna identiek aan `JdbcStudentDao`? Hoe zou je in een groter project met tientallen entiteiten de hoeveelheid herhaalde code beperken?

8. Het DAO-pattern dat je hier hebt gebouwd is de basis van wat frameworks als Hibernate en Spring Data voor je doen. Als je kijkt naar de code die je hebt geschreven, welke delen zou je het liefst automatisch laten genereren, en waarom?

---