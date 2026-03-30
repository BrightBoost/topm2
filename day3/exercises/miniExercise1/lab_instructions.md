# Mini Exercise: Hibernate Entity Mapping

- Use the provided Hibernate + H2 starter project
- Create a `Course` entity with annotations: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`
- Decide which fields need explicit `@Column(name = "...")` and which can use default mapping
- Start the `SessionFactory` and verify the `courses` table was created automatically
- Compare: how much code did you need for the same result on day 1?

---

# Lab: Je eerste Hibernate entity

## Scenario / Context

Je bent net begonnen als Java developer bij een onderwijsinstelling. Tot nu toe heb je met raw JDBC gewerkt: SQL met de hand geschreven, ResultSets handmatig gemapt, elke kolom zelf uitgelezen. Je teamlead heeft besloten dat het tijd is om over te stappen op Hibernate. Ze wil dat je begint met een simpele proof of concept: neem de `Course`-entiteit en map die met Hibernate-annotaties in plaats van handmatige SQL. Als het goed gaat, hoef je geen enkele regel DDL meer te schrijven — Hibernate genereert de tabel voor je. Klinkt bijna te mooi om waar te zijn, toch?

---

## Learning Goals

- Een Java-klasse als Hibernate entity configureren met `@Entity`, `@Table`, `@Id`, `@GeneratedValue` en `@Column`
- Beoordelen welke velden een expliciete `@Column(name = "...")` nodig hebben en welke gebruik kunnen maken van Hibernate's default mapping
- De `SessionFactory` opstarten en verifiëren dat Hibernate automatisch de bijbehorende tabel aanmaakt in de H2-database
- De gegenereerde DDL in de console vergelijken met de handmatige DDL die je schreef op dag 1
- Beredeneren hoeveel boilerplate-code Hibernate elimineert ten opzichte van raw JDBC

---

## Prerequisites

- Java 21 geïnstalleerd
- Maven geïnstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Basiskennis van annotaties in Java
- De dag 1 & 2 exercises afgerond (JDBC, DAO pattern, mini-ORM)
- Het starter project `hibernate-exercises` geopend in je IDE

---

# Lab Parts

Dit lab bevat **3 delen**.

---

## Part 1: Het starter project verkennen

### What you will do

Open het meegeleverde starter project (`hibernate-exercises`). Bekijk de `pom.xml` om te zien welke dependencies er zijn (Hibernate, H2, SLF4J). Open de `HibernateUtil`-klasse en bestudeer hoe de `SessionFactory` programmatisch wordt geconfigureerd. Let op de properties: database-URL, dialect, `show_sql`, en `hbm2ddl.auto`. Je hoeft nog niets te coderen — dit is verkenning.

### Success criteria

- Je kunt de drie hoofddependencies in de `pom.xml` benoemen (Hibernate Core, H2, SLF4J)
- Je begrijpt wat `hbm2ddl.auto = create` doet (tabellen aanmaken bij elke start)
- Je begrijpt wat `show_sql = true` doet (gegenereerde SQL in de console tonen)
- Je kunt uitleggen waarom de `SessionFactory` een singleton is

### Hints

<details>
<summary>Hint 1</summary>

Kijk in de `pom.xml` onder het `<dependencies>`-blok. Elke dependency heeft een `groupId` en `artifactId` die je vertellen wat het is.

</details>

<details>
<summary>Hint 2</summary>

In `HibernateUtil` wordt de `SessionFactory` aangemaakt via een `Configuration`-object. De properties worden programmatisch gezet in een `Properties`-object. Zoek naar `Environment.HBM2DDL_AUTO` en `Environment.SHOW_SQL`.

</details>

<details>
<summary>Hint 3</summary>

`hbm2ddl.auto = create` betekent: Hibernate dropt alle tabellen en maakt ze opnieuw aan bij elke start. Handig voor development en tests, maar catastrofaal voor productie (al je data is weg!).

</details>

---

## Part 2: De Course entity aanmaken

### What you will do

Maak een nieuwe Java-klasse `Course` in het package `com.example.model`. Voeg de juiste Hibernate/JPA-annotaties toe: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, en waar nodig `@Column`. De entity moet de volgende velden hebben:

- `id` (Long, auto-gegenereerd)
- `name` (String)
- `credits` (int)
- `description` (String, gemapt op kolom `course_description`)

Denk na over welke velden een expliciete `@Column` nodig hebben. Als het Java-veld dezelfde naam heeft als de gewenste databasekolom, kan Hibernate het zelf uitvogelen (default mapping).

### Success criteria

- De klasse heeft de annotatie `@Entity`
- De klasse heeft `@Table(name = "courses")`
- Het `id`-veld heeft `@Id` en `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- Het `description`-veld heeft `@Column(name = "course_description")` omdat de kolomnaam afwijkt van de veldnaam
- De velden `name` en `credits` hebben **geen** expliciete `@Column` (default mapping)
- De klasse heeft een no-arg constructor (vereist door Hibernate)
- De klasse heeft getters en setters voor alle velden

### Hints

<details>
<summary>Hint 1</summary>

Hibernate heeft altijd een no-arg constructor nodig om objecten via reflectie aan te maken. Voeg een lege `public Course() {}` toe, naast een constructor met parameters als je die handig vindt.

</details>

<details>
<summary>Hint 2</summary>

De annotaties komen uit het `jakarta.persistence`-package, niet uit Hibernate-specifieke packages. Gebruik `import jakarta.persistence.*;` om ze allemaal in één keer te importeren.

</details>

<details>
<summary>Hint 3</summary>

Alleen als de kolomnaam in de database afwijkt van de Java-veldnaam heb je `@Column(name = "...")` nodig. Voor `description` → `course_description` is dat het geval. Voor `name` → `name` niet.

</details>

<details>
<summary>Hint 4</summary>

```java
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int credits;

    @Column(name = "course_description")
    private String description;

    public Course() {}

    public Course(String name, int credits, String description) {
        this.name = name;
        this.credits = credits;
        this.description = description;
    }

    // getters en setters...
}
```

</details>

---

## Part 3: De SessionFactory starten en de tabel verifiëren

### What you will do

Registreer je `Course`-entity in de `HibernateUtil`-klasse (via `configuration.addAnnotatedClass(Course.class)`). Maak vervolgens een `Main`-klasse met een `main`-methode die de `SessionFactory` opstart. Kijk in de console-output: Hibernate zou de `CREATE TABLE`-statement moeten printen. Vergelijk die met de DDL die je op dag 1 handmatig schreef.

### Success criteria

- De `Course`-klasse is geregistreerd in `HibernateUtil` met `addAnnotatedClass`
- De applicatie start zonder fouten
- In de console verschijnt een `CREATE TABLE courses`-statement (dankzij `show_sql = true`)
- De tabel heeft de kolommen: `id`, `name`, `credits`, `course_description`
- Je kunt benoemen hoeveel regels code dit kostte vergeleken met dag 1 (spoiler: aanzienlijk minder)

### Hints

<details>
<summary>Hint 1</summary>

In `HibernateUtil` zoek je de plek waar `addAnnotatedClass` wordt aangeroepen. Voeg daar `configuration.addAnnotatedClass(Course.class)` toe. Vergeet de import niet.

</details>

<details>
<summary>Hint 2</summary>

Je `Main`-klasse hoeft alleen de SessionFactory op te starten. Dat is genoeg om de tabelcreatie te triggeren:

```java
public class Main {
    public static void main(String[] args) {
        var sessionFactory = HibernateUtil.getSessionFactory();
        System.out.println("SessionFactory created successfully!");
        HibernateUtil.shutdown();
    }
}
```

</details>

<details>
<summary>Hint 3</summary>

Als je een foutmelding krijgt over een ontbrekende no-arg constructor, controleer dan of je `Course`-klasse een `public Course() {}` heeft. Hibernate kan geen objecten aanmaken zonder.

</details>

<details>
<summary>Hint 4</summary>

Vergelijk de output in de console met wat je op dag 1 deed. Op dag 1 schreef je zoiets als:

```sql
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    credits INT,
    course_description VARCHAR(255)
);
```

Hibernate genereert dit nu automatisch op basis van je annotaties. Hoeveel regels Java-code had je op dag 1 nodig om dezelfde tabel te maken en te vullen?

</details>

---

## Reflectievragen

1. Welke velden hadden een expliciete `@Column`-annotatie nodig en waarom?
2. Wat zou er gebeuren als je de `@Table`-annotatie weglaat — welke tabelnaam zou Hibernate dan gebruiken?
3. Hoeveel boilerplate-code (SQL, mapping, connectiebeheer) elimineert Hibernate vergeleken met je dag 1 aanpak?
4. Wat zijn mogelijke risico's van `hbm2ddl.auto = create` in een productieomgeving?
