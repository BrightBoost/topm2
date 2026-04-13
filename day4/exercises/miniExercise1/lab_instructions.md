# Mini Exercise 1: Van tekst naar diagram — een universiteitsdomein modelleren

## Scenario / Context

Je bent aangenomen als software architect bij een universiteit die haar administratiesysteem wil moderniseren. Het huidige systeem is een wirwar van Excel-sheets en losse Access-databases. De opdrachtgever heeft je een beschrijving gegeven van het domein: studenten, cursussen, docenten, afdelingen — het hele circus. Voordat je ook maar één regel code schrijft, wil je team een helder datamodel. Je taak: vertaal de beschrijving naar een UML-klassendiagram dat iedereen begrijpt, van de product owner tot de junior developer die volgende week begint.

---

## Learning Goals

- Een domeinbeschrijving in tekst vertalen naar een UML-klassendiagram 
- Multipliciteiten bepalen en noteren voor alle relaties in het domein (`1`, `0..1`, `*`, `1..*`)
- Beredeneren welke relaties compositie zijn en welke aggregatie, met onderbouwing van de keuze
- Evalueren of een many-to-many relatie beter gemodelleerd kan worden als een join-entiteit met eigen attributen
- Navigabiliteit analyseren en argumenteren welke kant van een relatie een referentie nodig heeft
- Het eigen diagram vergelijken met dat van een medestudent en afwijkingen bespreken

---

## Prerequisites

- De theorie over UML-klassendiagrammen uit blok 1 gevolgd
- Basiskennis van relaties tussen entiteiten (one-to-many, many-to-many, one-to-one)
- Pen en papier, of een tool zoals draw.io / Excalidraw / whiteboard
- Kennis van de Hibernate-entiteiten uit dag 3 (`Course`, `OnlineCourse`, `ClassroomCourse`)

---

# Lab Parts

Dit lab bevat **3 delen**.

---

## Part 1: De entiteiten identificeren en tekenen

### What you will do

Lees de domeinbeschrijving hieronder en identificeer alle entiteiten. Teken voor elke entiteit een UML-klasse-box met de naam bovenaan en de belangrijkste attributen eronder. Denk alvast na over welke entiteiten er "verstopt" zitten in de beschrijving.

**Domeinbeschrijving:**

> De universiteit heeft meerdere afdelingen (departments). Elke afdeling heeft een naam en biedt meerdere cursussen aan. Een cursus heeft een naam, een aantal studiepunten en een beschrijving. Elke cursus wordt gegeven door precies één docent (professor), maar een docent kan meerdere cursussen geven. Een docent hoort bij één afdeling.
>
> Studenten hebben een naam, studentnummer en e-mailadres. Een student kan zich inschrijven voor meerdere cursussen, en elke cursus kan meerdere studenten hebben. Bij elke inschrijving wordt de inschrijfdatum vastgelegd, en er kan optioneel een cijfer aan gekoppeld zijn.
>
> Elke student heeft precies één studentprofiel met daarin een adres en telefoonnummer.

### Success criteria

- Er zijn minimaal 7 entiteiten geïdentificeerd
- Elke entiteit is getekend als een UML-klasse-box met naam en attributen
- De attributen per entiteit zijn relevant en beknopt (geen implementatiedetails zoals getters/setters)
- Relaties komen in part 2

### Hints

<details>
<summary>Hint 1</summary>

Lees de beschrijving zin voor zin. Elk zelfstandig naamwoord is een potentiële entiteit. Maar let ook op relaties met eigen data — "inschrijving met een datum en cijfer" is geen simpele lijn, dat is een aparte entiteit.

</details>

<details>
<summary>Hint 2</summary>

De beschrijving noemt geen `University` expliciet, maar het is de container van alles. Voeg deze toe als top-level entiteit. Hetzelfde geldt voor `Enrollment` — de beschrijving zegt "inschrijving" met eigen attributen (datum, cijfer), dus dat verdient een eigen box.

</details>

<details>
<summary>Hint 3</summary>

Houd de attributen simpel. Voor `Student`: `name`, `studentNumber`, `email`. Geen `id` nodig in het UML-diagram (dat is een implementatiedetail), maar het mag wel. Laat methodes weg — we modelleren data, geen gedrag.

</details>

<details>
<summary>Hint 4</summary>

Een overzicht van de entiteiten en hun kernattributen:

- `University`: `name`
- `Department`: `name`
- `Course`: `name`, `credits`, `description`
- `Professor`: `name`, `employeeNumber`
- `Student`: `name`, `studentNumber`, `email`
- `Enrollment`: `enrollmentDate`, `grade` (optioneel)
- `StudentProfile`: `address`, `phoneNumber`

</details>

---

## Part 2: Relaties, multipliciteiten en diamanten

### What you will do

Verbind de entiteiten met relatielijnen. Noteer bij elke lijn de multipliciteit aan beide kanten. Gebruik gevulde diamanten (compositie) en open diamanten (aggregatie) waar van toepassing. Geef met pijlen aan welke kant van de relatie navigeerbaar is.

### Success criteria

- Alle relaties uit de domeinbeschrijving zijn getekend met correcte multipliciteiten
- Minimaal één compositie-relatie is aangeduid met een gevulde diamant (bijv. University → Department)
- Minimaal één aggregatie of gewone associatie is aangeduid (bijv. Professor → Course)
- Navigabiliteit is aangegeven met pijlen (niet elke relatie hoeft bidirectioneel)
- De multipliciteiten zijn logisch en uit te leggen

### Hints

<details>
<summary>Hint 1</summary>

Begin met de duidelijkste relaties. "Een afdeling biedt meerdere cursussen aan" = Department `1` ——— `*` Course. "Een student kan zich inschrijven voor meerdere cursussen" gaat via Enrollment: Student `1` ——— `*` Enrollment `*` ——— `1` Course.

</details>

<details>
<summary>Hint 2</summary>

Compositie (gevulde diamant) gebruik je wanneer het kind niet kan bestaan zonder de ouder. Als de universiteit ophoudt te bestaan, bestaan de afdelingen ook niet meer → compositie. Als een student stopt, bestaan de cursussen nog steeds → geen compositie.

</details>

<details>
<summary>Hint 3</summary>

Navigabiliteit: denk na over wie wie moet kennen. Een `Student` wil waarschijnlijk zijn inschrijvingen weten, maar een `Enrollment` moet ook weten bij welke student die hoort (voor de foreign key). Dat maakt het bidirectioneel. Maar moet een `Course` echt weten bij welke `University` die hoort? Misschien alleen via `Department`.

</details>

<details>
<summary>Hint 4</summary>

Voorbeeld van te verdedigen relaties:

| Van        | Naar           | Type        | Multipliciteit | Compositie/Aggregatie? |
| ---------- | -------------- | ----------- | -------------- | ---------------------- |
| University | Department     | one-to-many | `1` — `*`      | Compositie (gevuld)    |
| Department | Course         | one-to-many | `1` — `*`      | Compositie (gevuld)    |
| Professor  | Department     | many-to-one | `*` — `1`      | Geen                   |
| Professor  | Course         | one-to-many | `1` — `*`      | Geen                   |
| Student    | Enrollment     | one-to-many | `1` — `*`      | Compositie (gevuld)    |
| Course     | Enrollment     | one-to-many | `1` — `*`      | Geen                   |
| Student    | StudentProfile | one-to-one  | `1` — `1`      | Compositie (gevuld)    |

</details>

---

## Part 3: Vergelijken en bespreken

### What you will do

Vergelijk je diagram met dat van een medestudent. Zoek naar verschillen in modellering: andere multipliciteiten, andere navigabiliteit, compositie vs. aggregatie. Bespreek welke keuzes beter zijn en waarom. Beantwoord samen de volgende vragen:

1. Hebben jullie `Enrollment` als aparte entiteit gemodelleerd, of als directe many-to-many? Wat zijn de voor- en nadelen?
2. Welke relaties hebben jullie bidirectioneel gemaakt? Is dat nodig, of kan het simpeler?
3. Zijn er plekken waar jullie overerving zouden toepassen? (Denk aan `OnlineCourse` en `ClassroomCourse` van dag 3.)

### Success criteria

- Je hebt je diagram vergeleken met minimaal één medestudent
- Je kunt per verschil beargumenteren welke keuze sterker is (of waarom beide valide zijn)
- Je hebt nagedacht over de trade-off van `Enrollment` als join-entiteit vs. directe many-to-many

### Hints

<details>
<summary>Hint 1</summary>

Er is geen "perfect" UML-diagram. Verschillende modellen kunnen allebei correct zijn, afhankelijk van de use case. Het doel is dat je keuzes kunt verantwoorden.

</details>

<details>
<summary>Hint 2</summary>

Een directe `@ManyToMany` tussen Student en Course werkt prima als je geen extra informatie per inschrijving nodig hebt. Maar zodra je een datum of cijfer wilt opslaan, heb je een join-entiteit (`Enrollment`) nodig. In de praktijk komt de join-entiteit veel vaker voor dan je zou verwachten.

</details>

<details>
<summary>Hint 3</summary>

Over bidirectionaliteit: in Hibernate vertaalt een bidirectionele relatie zich naar een `mappedBy`-constructie. Dat is extra code en extra onderhoud. Unidirectioneel is simpeler, maar soms heb je aan beide kanten een referentie nodig. Vuistregel: maak het unidirectioneel tenzij je een goede reden hebt voor bidirectioneel.

</details>

---

# Bonus Challenge (Optional)

Voeg overerving toe aan je diagram. Maak `OnlineCourse` en `ClassroomCourse` als subtypes van `Course`, elk met een eigen attribuut (`platformUrl` voor online, `roomNumber` voor klassikaal). Gebruik de UML-notatie voor inheritance (een lijn met een holle pijlpunt naar de superklasse). Bedenk: welke inheritance-strategie (Single Table, Table Per Class) zou je kiezen als je dit in Hibernate implementeert, en waarom?

---

# Reflectievragen

### Implementatie & Trade-offs

1. Je hebt gekozen voor compositie of aggregatie bij verschillende relaties. Bij welke relatie was die keuze het moeilijkst, en wat gaf de doorslag?
2. Wat zijn de concrete consequenties van `Enrollment` als join-entiteit versus een directe `@ManyToMany`? In welk scenario zou je toch voor `@ManyToMany` kiezen?

### Production Readiness

3. Als dit diagram de basis wordt voor een echte database, welke relatie zou de meeste data bevatten? Hoe beïnvloedt dat je keuze voor eager of lazy loading (alvast vooruitkijkend naar blok 3)?
4. Stel je voor dat er later een nieuwe requirement komt: een student kan dezelfde cursus twee keer volgen (bijv. na een onvoldoende). Ondersteunt je huidige model dat? Zo niet, wat moet je aanpassen?

### Debugging & Problem Solving

5. Was er een relatie waarvan je twijfelde of het one-to-many of many-to-many moest zijn? Hoe heb je die knoop doorgehakt?
6. Bij het vergelijken met je medestudent: welk verschil verraste je het meest, en heeft het je van gedachten doen veranderen?

### Adaptatie / Transfer

7. Hoe zou dit diagram er anders uitzien als je het voor een middelbare school modelleerde in plaats van een universiteit? Welke entiteiten blijven, welke veranderen?
8. Als je dit domein zou modelleren als een REST API in plaats van een database, zou je dan dezelfde entiteiten en relaties gebruiken? Waar zou je afwijken?

---

