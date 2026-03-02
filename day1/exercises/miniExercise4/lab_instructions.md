# Lab: Van DriverManager naar DataSource

## Scenario 

Je StudentDao werkt, je CRUD-operaties draaien als een zonnetje, en je bent terecht trots. Maar je technisch lead kijkt over je schouder en zegt: "Leuk, maar in productie gaat dit niet vliegen. Elke keer een nieuwe connectie openen via `DriverManager`? Dat is alsof je voor elk telefoontje opnieuw het nummer moet intoetsen." Ze wijst je naar `DataSource` — de standaard manier om connecties te beheren in professionele Java-applicaties. Dus... Tijd om je DAO te refactoren.

---

## Learning Goals

- Een `JdbcDataSource` configureren als vervanging voor `DriverManager.getConnection()`
- Een bestaande DAO-klasse refactoren om een `DataSource` te ontvangen via de constructor
- Beoordelen waarom/wanneer `DataSource` de voorkeur heeft boven `DriverManager` in termen van herbruikbaarheid, testbaarheid en schaalbaarheid
- Een centraal configuratiepunt ontwerpen voor database-instellingen en evalueren hoe dit onderhoudbaarheid verbetert

---

## Prerequisites

- Java 21 geinstalleerd
- Maven geinstalleerd
- Een IDE (IntelliJ IDEA aanbevolen)
- Mini Exercise 3 afgerond (werkende `JdbcStudentDao` met `DriverManager`)
- Begrijpen hoe `DriverManager.getConnection()` werkt

---

# Lab Parts

Dit lab bevat **3 delen**, plus een bonusopdracht.

---

## Part 1: Een DataSource aanmaken

### What you will do

Vervang de losse `DriverManager.getConnection(url, user, password)` aanroep door een geconfigureerde `JdbcDataSource` van H2. Je maakt een `DataSource`-object aan in je `main`-methode en configureert daar de database-URL, gebruiker en wachtwoord.

Open het project in de map `datasource-exercise` en bekijk het bestand `DataSourceApp.java`. Vul de TODOs in om een `JdbcDataSource` aan te maken.

### Success criteria

- Een `JdbcDataSource` is aangemaakt met de juiste URL, user en password
- Je kunt via `dataSource.getConnection()` een connectie openen
- De connectie-test print een bevestiging naar de console (bijv. "Connectie succesvol!")

### Hints

<details>
<summary>Hint 1</summary>

De klasse die je nodig hebt is `org.h2.jdbcx.JdbcDataSource`. Deze zit in de H2 dependency die je al hebt.

</details>

<details>
<summary>Hint 2</summary>

Een `JdbcDataSource` heeft setter-methodes: `setURL()`, `setUser()` en `setPassword()`. Gebruik dezelfde waarden als je eerder bij `DriverManager` gebruikte.

</details>

<details>
<summary>Hint 3</summary>

Je kunt testen of de DataSource werkt door een connectie te openen en meteen weer te sluiten:

```java
try (Connection conn = dataSource.getConnection()) {
    System.out.println("Connectie succesvol!");
}
```

</details>

<details>
<summary>Hint 4</summary>

```java
JdbcDataSource dataSource = new JdbcDataSource();
dataSource.setURL("jdbc:h2:file:./data/studentdb");
dataSource.setUser("sa");
dataSource.setPassword("");
```

Let op: `setURL()` met hoofdletters, niet `setUrl()`.

</details>

---

## Part 2: JdbcStudentDao refactoren naar DataSource

### What you will do

Pas de `JdbcStudentDao` aan zodat deze een `javax.sql.DataSource` ontvangt in de constructor, in plaats van losse url/user/password Strings. De interne `getConnection()` methode haalt nu de connectie op via de DataSource.

### Success criteria

- De constructor van `JdbcStudentDao` accepteert een `DataSource` parameter
- De velden `url`, `user` en `password` zijn verdwenen
- De private `getConnection()` methode gebruikt `dataSource.getConnection()`
- Alle bestaande CRUD-methodes werken nog zonder wijzigingen in hun logica

### Hints

<details>
<summary>Hint 1</summary>

Het enige dat verandert in `JdbcStudentDao` is hoe connecties worden opgehaald. De SQL-queries, PreparedStatements en ResultSet-verwerking blijven identiek.

</details>

<details>
<summary>Hint 2</summary>

Vervang de drie velden (`url`, `user`, `password`) door een enkel `DataSource`-veld. Pas de constructor aan om een `DataSource` te ontvangen.

</details>

<details>
<summary>Hint 3</summary>

De `getConnection()` methode wordt een stuk simpeler:

```java
private Connection getConnection() throws SQLException {
    return dataSource.getConnection();
}
```

</details>

<details>
<summary>Hint 4</summary>

De volledige refactoring van de relevante delen:

```java
public class JdbcStudentDao implements StudentDao {

    private final DataSource dataSource;

    public JdbcStudentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Alle andere methodes (findAll, findById, save, update, delete)
    // blijven EXACT hetzelfde — ze roepen getConnection() aan
    // en dat werkt nu via de DataSource.
}
```

</details>

---

## Part 3: Alles verifiëren

### What you will do

Werk `DataSourceApp.main()` bij zodat de `JdbcStudentDao` wordt aangemaakt met de nieuwe `DataSource`, en voer dezelfde testsequentie uit als in Mini Exercise 3: studenten opslaan, ophalen, bijwerken en verwijderen.

### Success criteria

- De applicatie start zonder fouten
- Studenten worden succesvol opgeslagen, opgehaald, bijgewerkt en verwijderd
- De console-output toont dezelfde resultaten als in Mini Exercise 3
- De hele applicatie gebruikt nergens meer `DriverManager` direct

### Hints

<details>
<summary>Hint 1</summary>

De enige plek waar de `DataSource` wordt aangemaakt is in `main()`. Daarna wordt het object doorgegeven aan de DAO. Dit is een simpele vorm van dependency injection.

</details>

<details>
<summary>Hint 2</summary>

Kopieer je testcode uit Mini Exercise 3 en pas alleen de DAO-constructie aan. De rest van je testcode hoeft niet te veranderen, want het `StudentDao`-interface is hetzelfde gebleven.

</details>

<details>
<summary>Hint 3</summary>

```java
// DataSource configureren
JdbcDataSource dataSource = new JdbcDataSource();
dataSource.setURL("jdbc:h2:file:./data/studentdb");
dataSource.setUser("sa");
dataSource.setPassword("");

// DAO aanmaken met DataSource
JdbcStudentDao dao = new JdbcStudentDao(dataSource);

// Verder precies hetzelfde als voorheen
dao.createTable();
dao.save(new Student("Alice", "alice@uni.nl", 21));
// etc.
```

</details>

---

# Bonus Challenge (Optional)

### DatabaseConfig utility class

Maak een utility class `DatabaseConfig` die de verantwoordelijkheid voor het aanmaken en configureren van de `DataSource` centraliseert. De class heeft een static methode `createDataSource()` die een volledig geconfigureerde `DataSource` teruggeeft.

Denk na over deze vragen:

- Waar komen de configuratiewaarden (URL, user, password) vandaan? Hardcoded, of uit een properties-bestand?
- Wat als je later overstapt van H2 naar PostgreSQL? Hoeveel code moet je dan aanpassen?
- Hoe zorg je ervoor dat er maar een `DataSource`-instantie wordt aangemaakt in je hele applicatie?

Als je wilt, lees de configuratie uit een `database.properties` bestand met `Properties.load()`. Zo kun je van database wisselen zonder code te wijzigen.

---

# Reflection Questions

### Implementation & Trade-offs

1. Je hebt `DriverManager` vervangen door `DataSource`. De methodes in je DAO zijn exact hetzelfde gebleven. Wat zegt dat over de kwaliteit van je oorspronkelijke ontwerp met de private `getConnection()` methode?

2. De constructor van `JdbcStudentDao` accepteert nu een `DataSource` in plaats van drie losse Strings. Wat zijn de voordelen van het injecteren van een object ten opzichte van primitieve configuratiewaarden?

### Production Readiness

3. In een productieomgeving zou je een connection pool gebruiken (bijv. HikariCP) in plaats van de simpele `JdbcDataSource`. Wat denk je dat er gebeurt met de performance als 100 gebruikers tegelijk connecties aanvragen zonder pooling?

4. De database-URL en credentials staan nu hardcoded in je code. Welke risico's brengt dat met zich mee en hoe zou je dit in een productiesysteem oplossen?

### Debugging & Problem Solving

5. Stel: na de refactoring compileert alles, maar bij het uitvoeren krijg je een `NullPointerException` in `getConnection()`. Wat is de meest waarschijnlijke oorzaak?

6. Je hebt per ongeluk `setURL()` aangeroepen met een lege string. Welke foutmelding verwacht je, en op welk moment in de executie zou die optreden?

### Adaptation / Transfer

7. Het patroon dat je hier toepast — een afhankelijkheid via de constructor injecteren in plaats van intern aanmaken — heet dependency injection. Waar kom je dit patroon nog meer tegen in Java-applicaties, en waarom is het zo populair?

8. Als je straks een connection pool library als HikariCP wilt gebruiken, hoeveel code moet je dan aanpassen in `JdbcStudentDao`? En hoeveel in `DataSourceApp`? Wat leert je dat over de waarde van het programmeren tegen een interface (`DataSource`)?

---
