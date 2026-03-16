# Lab: Welk Isolation Level Past bij het Scenario?

## Scenario / Context

Je bent tech lead bij een onderwijsplatform dat snel groeit. De applicatie heeft steeds meer last van concurrency-problemen: hier en daar verschijnen vreemde getallen in rapporten, dubbele inschrijvingen sluipen erin, en een collega klaagt dat haar query telkens een ander resultaat geeft binnen dezelfde berekening. De DBA vraagt je om voor vier concrete situaties het juiste isolation level te kiezen. Er is geen code om te schrijven — dit is een denkoefening. Het gaat om het begrijpen van de trade-off tussen correctheid en performance, en het herkennen welk concurrency-probleem bij welk scenario hoort.

---

## Learning Goals

- De vier SQL-standaard isolation levels benoemen en rangschikken op striktheid
- Dirty reads, non-repeatable reads en phantom reads herkennen in concrete scenario's
- Het juiste isolation level selecteren op basis van de vereisten van een scenario
- De trade-off tussen performance en correctheid beredeneren voor elk gekozen isolation level
- Beargumenteren waarom een strenger of minder streng isolation level ongeschikt zou zijn in een gegeven situatie

---

## Prerequisites

- Kennis van de vier isolation levels (READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE)
- Kennis van dirty reads, non-repeatable reads en phantom reads
- De slides over isolation levels doorgenomen

---

# Lab Parts

Dit lab bevat **4 delen**. Elk deel beschrijft een scenario. Jouw taak: kies het meest geschikte isolation level en beargumenteer waarom.

---

## Part 1: Het dashboard met "live" statistieken

### Scenario

Het platform heeft een intern dashboard dat elke 10 seconden het totaal aantal actieve studenten toont. Dit getal wordt gebruikt door het management om een globale indruk te krijgen — het hoeft niet tot op de student nauwkeurig te zijn. Performance is belangrijk, want de query draait over miljoenen rijen. Als het dashboard af en toe een getal toont dat gebaseerd is op nog niet ge-committe data, is dat acceptabel.

### Success criteria

- Je hebt een isolation level gekozen
- Je kunt uitleggen welk concurrency-probleem je accepteert en waarom dat hier oké is
- Je kunt uitleggen waarom een strenger level onnodig zou zijn

### Hints

<details>
<summary>Hint 1</summary>

Welke concurrency-problemen zijn er ook alweer? Dirty reads, non-repeatable reads, phantom reads. Welke zijn hier een probleem, en welke niet?

</details>

<details>
<summary>Hint 2</summary>

Het scenario zegt expliciet dat het getal niet exact hoeft te zijn en dat ongecommitte data acceptabel is. Welk isolation level laat dat toe?

</details>

<details>
<summary>Hint 3</summary>

Als je dirty reads accepteert, zit je op het laagste niveau. Kijk naar de tabel uit de slides — welk level staat dirty reads toe?

</details>

<details>
<summary>Hint 4 — Antwoord</summary>

**READ_UNCOMMITTED** is hier het juiste antwoord.

Waarom? Het dashboard toont een grove indicatie, geen financieel rapport. Het mag af en toe een getal laten zien dat gebaseerd is op een nog niet ge-committe wijziging — dat is per definitie een dirty read, en die accepteer je hier bewust. READ_UNCOMMITTED is het snelste level omdat de database geen enkele isolatiegarantie hoeft af te dwingen: geen locks, geen versioning overhead. Voor een query over miljoenen rijen die elke 10 seconden draait, maakt dat verschil.

Een strenger level (zoals READ_COMMITTED) zou dirty reads voorkomen, maar dat levert hier geen meerwaarde op — het kost alleen maar meer resources voor een getal dat toch al een benadering is.

</details>

---

## Part 2: Het inschrijfoverzicht van een student

### Scenario

Een student opent haar profiel en bekijkt haar inschrijvingen. De pagina toont een lijst van cursussen en het totaal aantal studiepunten. De applicatie voert twee queries uit binnen dezelfde transactie: één om de cursussen op te halen, en één om de studiepunten op te tellen. Het is belangrijk dat deze twee queries consistent zijn — als een cursus meetelt in de lijst, moeten de studiepunten dat ook weerspiegelen. Het is niet erg als een andere student op datzelfde moment een cursus toevoegt die pas bij de volgende keer laden zichtbaar is, maar wel erg als de data halverwege verandert.

### Success criteria

- Je hebt een isolation level gekozen
- Je kunt uitleggen welk concurrency-probleem je wilt voorkomen
- Je kunt uitleggen waarom een level lager te risicovol zou zijn

### Hints

<details>
<summary>Hint 1</summary>

Het scenario heeft twee reads binnen dezelfde transactie die consistent moeten zijn. Wat gebeurt er als tussen die twee reads een andere transactie data wijzigt en commit?

</details>

<details>
<summary>Hint 2</summary>

Als dezelfde rij een andere waarde geeft bij een tweede read, is dat een non-repeatable read. Welk isolation level voorkomt dat?

</details>

<details>
<summary>Hint 3</summary>

Het scenario zegt dat nieuwe rijen (phantom reads) acceptabel zijn — die zijn pas bij de volgende pageload zichtbaar. Je hoeft dus niet helemaal naar SERIALIZABLE te gaan.

</details>

<details>
<summary>Hint 4 — Antwoord</summary>

**REPEATABLE_READ** is hier het juiste antwoord.

Waarom? De student verwacht dat haar profielpagina een consistent beeld toont. Als de eerste query 5 cursussen teruggeeft en de tweede query de studiepunten berekent, dan moeten die twee kloppen met elkaar. Bij READ_COMMITTED zou een andere transactie tussenin een cursus kunnen updaten (bijvoorbeeld de studiepunten wijzigen), waardoor de tweede query een ander totaal geeft dan je op basis van de eerste query zou verwachten. Dat is een non-repeatable read, en die wil je hier voorkomen.

REPEATABLE_READ garandeert dat data die je eenmaal hebt gelezen niet verandert gedurende je transactie. Phantom reads (nieuwe rijen die verschijnen) zijn nog steeds mogelijk, maar het scenario zegt expliciet dat dat acceptabel is. SERIALIZABLE zou overkill zijn: het voorkomt phantoms die hier geen probleem vormen, tegen een hogere performance-kostprijs.

</details>

---

## Part 3: De salarisberekening

### Scenario

Aan het eind van de maand draait een batch-job die het salaris berekent voor alle docenten. De berekening is complex: het script leest het basissalaris, telt overuren/bonussen op uit een aparte tabel, trekt inhoudingen af, en schrijft het resultaat weg. Het is absoluut onacceptabel dat tijdens deze berekening nieuwe bonusregels verschijnen of dat bedragen veranderen. Als de batch-job een docent verwerkt, moet de database er exact zo uitzien als aan het begin van de transactie — geen wijzigingen, geen nieuwe rijen, niets. Correctheid gaat hier boven alles; de job draait 's nachts, dus performance is minder belangrijk.

### Success criteria

- Je hebt een isolation level gekozen
- Je kunt uitleggen welke concurrency-problemen je allemaal wilt voorkomen
- Je kunt uitleggen waarom iets minder streng hier een risico zou zijn

### Hints

<details>
<summary>Hint 1</summary>

Het scenario zegt: geen wijzigingen, geen nieuwe rijen. Dat sluit out welke concurrency-problemen je moet voorkomen?

</details>

<details>
<summary>Hint 2</summary>

Je moet dirty reads, non-repeatable reads én phantom reads voorkomen. Welk isolation level doet dat allemaal?

</details>

<details>
<summary>Hint 3</summary>

Performance is geen issue (de job draait 's nachts). Dat haalt het belangrijkste nadeel van het strengste level weg.

</details>

<details>
<summary>Hint 4 — Antwoord</summary>

**SERIALIZABLE** is hier het juiste antwoord.

Waarom? Salarisberekeningen zijn financieel en moeten 100% correct zijn. Het scenario eist dat er tijdens de berekening geen data verandert (non-repeatable reads) én geen nieuwe rijen verschijnen (phantom reads). Alleen SERIALIZABLE garandeert beide. Bij REPEATABLE_READ zouden er nog steeds nieuwe bonusregels kunnen verschijnen (phantom reads) terwijl je midden in de berekening zit, wat tot een fout salaris zou leiden.

Het grote nadeel van SERIALIZABLE — meer locks, lagere throughput, kans op deadlocks — is hier irrelevant omdat de batch-job 's nachts draait wanneer er geen concurrerende gebruikers zijn. Dit is precies het type scenario waarvoor SERIALIZABLE bedoeld is: correctheid is absoluut kritisch en performance is secundair.

</details>

---

## Part 4: De cursuscatalogus

### Scenario

Studenten bladeren door de cursuscatalogus — een lijst van alle beschikbare cursussen met beschrijving en het aantal beschikbare plekken. De pagina wordt constant bekeken door honderden studenten tegelijk. De data hoeft niet real-time te zijn, maar het is wel belangrijk dat studenten geen cursusinformatie zien die nooit ge-commit is (een docent die een cursus aanpast maar de wijziging terugdraait, mag niet zichtbaar zijn). Het is oké als het aantal plekken een paar seconden achterloopt, en het is ook oké als bij het herladen van de pagina andere resultaten verschijnen. De catalogus is read-heavy en performance is kritisch.

### Success criteria

- Je hebt een isolation level gekozen
- Je kunt uitleggen welk concurrency-probleem je wilt voorkomen en welke je accepteert
- Je kunt uitleggen waarom dit level de beste balans biedt

### Hints

<details>
<summary>Hint 1</summary>

Het scenario zegt: geen ongecommitte data tonen. Welk concurrency-probleem is dat?

</details>

<details>
<summary>Hint 2</summary>

Dirty reads moeten voorkomen worden. Maar non-repeatable reads en phantom reads zijn acceptabel (het is oké als data verandert bij herladen). Welk isolation level voorkomt precies dirty reads en niets meer?

</details>

<details>
<summary>Hint 3</summary>

Dit is het meest gebruikte isolation level in productie. Het is de standaard van PostgreSQL en Oracle, en het is de Topicus-standaard.

</details>

<details>
<summary>Hint 4 — Antwoord</summary>

**READ_COMMITTED** is hier het juiste antwoord.

Waarom? De cursuscatalogus moet betrouwbare data tonen — geen half-aangepaste cursusinformatie van een transactie die misschien wordt teruggedraaid. Dat betekent: geen dirty reads. READ_COMMITTED garandeert precies dat: je ziet alleen data die ge-commit is.

Tegelijkertijd is het prima als het aantal plekken een paar seconden achterloopt (non-repeatable read) of als er bij herladen een cursus bijkomt (phantom read). READ_COMMITTED laat beide toe, en dat is hier geen probleem. Een strenger level zoals REPEATABLE_READ zou onnodige locks of snapshots vereisen, wat performance kost in een scenario met honderden gelijktijdige lezers. READ_COMMITTED biedt de beste balans tussen correctheid en performance — en het is niet voor niets de standaard bij de meeste databases en bij Topicus.

</details>

---

# Bonus Challenge (Optional)

Bedenk een vijfde scenario uit je eigen werkervaring (of verzin er een) waarin je twijfelt tussen twee isolation levels. Schrijf het scenario op en beargumenteer voor beide kanten. Bespreek het met een collega — zijn jullie het eens?

---

# Reflection Questions

### Implementation & Trade-offs

- In scenario 1 koos je het minst strikte level. Kun je een situatie bedenken waarin datzelfde dashboard wel een strenger level nodig zou hebben? Wat zou er dan anders zijn aan de requirements?
- Waarom is READ_COMMITTED de Topicus-standaard en niet REPEATABLE_READ? Welke trade-off zit daarachter?

### Production Readiness

- Stel dat de salarisberekening uit scenario 3 overdag zou draaien in plaats van 's nachts. Hoe zou dat je keuze beïnvloeden? Welke problemen kun je verwachten?
- Wat zou er in productie gebeuren als je per ongeluk READ_UNCOMMITTED gebruikt voor de cursuscatalogus? Hoe snel zou iemand het merken?

### Debugging & Problem Solving

- Een collega meldt dat studenten soms een verkeerd aantal studiepunten zien op hun profielpagina. Het probleem is niet reproduceerbaar. Welk concurrency-probleem vermoed je, en hoe zou je het bevestigen?
- Hoe zou je testen of een isolation level correct is ingesteld? Kun je dat met een unit test, of heb je iets anders nodig?

### Adaptation / Transfer

- Stel je hetzelfde type scenario's voor, maar dan in een microservices-architectuur waar data over meerdere services verdeeld is. Hoe verandert het concept van isolation levels als je niet meer één database hebt?
