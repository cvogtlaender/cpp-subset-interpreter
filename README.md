# Entwurf und Implementierung eines interaktiven Interpreter-Ökosystems für eine eingeschränkte C++-Teilmenge mit Typinferenz und LSP-Unterstützung

**Clemens Vogtländer, Dennis Gorpinic**

---

## Beschreibung

Das Projekt entwickelt ein vollständiges, interaktives Interpreter-Ökosystem für einen formal definierten C++-Subdialekt. Die Kernaufgabe basiert auf Blatt 08 der CB-Vorlesung und wird um professionelle Entwicklerwerkzeuge (LSP) und KI-Integration (MCP/GenAI) erweitert. Das System gliedert sich in vier Säulen:

1. **Kern-Interpreter** (gemeinsam): Lexer, Parser, AST, Resolver, Typprüfung, Tree-Walking-Interpreter und REPL.
2. **LSP-Server** (Clemens Vogtländer): Inkrementelle IDE-Integration über das Language Server Protocol 3.17, Zielplattform VS Code.
3. **MCP-Server** (Dennis Gorpinic): GenAI-Anbindung für Code-Assistenz (Vervollständigung, Refactoring, Bug-Detection) via Ollama (lokal).
4. **Evaluation**: Korrektheit, Performance und Skalierbarkeit.

### Getroffene Architekturentscheidungen

Die folgenden Entscheidungen wurden gemeinsam getroffen und als Architecture Decision Records (ADRs) festgehalten:

- **Implementierungssprache: Java** – Java bietet eine ausgereifte Standardbibliothek, starke IDE-Unterstützung und eignet sich gut für objektorientierte AST-Modellierung via Klassenhierarchien und dem Visitor-Pattern. Der Tree-Walking-Interpreter lässt sich sauber über Visitor-Klassen strukturieren.
- **Parser-Strategie: ANTLR4** – ANTLR4 ist der de-facto-Standard-Parser-Generator für die JVM; er generiert sowohl Lexer als auch Parser aus einer einheitlichen `.g4`-Grammatik. LL(*)-Parsing mit automatischer Fehlerbehandlung, umfangreiche Dokumentation und aktive Community.
- **LSP-Zielplattform: VS Code** – Die VS Code Extension API ist gut dokumentiert; `LSP4J` dient als Java-seitige LSP-Grundlage.
- **GenAI-Provider: Ollama (lokal)** – Kein API-Schlüssel erforderlich, keine Betriebskosten, keine Datenschutzprobleme. Empfohlene Modelle: `codellama` oder `deepseek-coder`.

---

## Anforderungen

### Sprachumfang

#### Typen und Variablen

Unterstützte Basistypen sind `bool`, `int`, `char`, `string` und `void`. Escape-Sequenzen mit `\` sind erlaubt. Variablen werden per `T x;` oder `T x = expr;` deklariert. C++-Referenzen werden als `T& x = expr;` (Variable) bzw. `T& p` (Parameter) unterstützt. Referenz-Initialisierung ist obligatorisch; Zuweisung schreibt in das referenzierte Ziel (keine Neubindung). Referenz-Felder, `&`-Rückgaben und globale Variablen sind nicht erlaubt.

#### Ausdrücke und Kontrollfluss

Arithmetische Operatoren (`+ - * / %`, unär `+ -`) sind auf `int` beschränkt. Vergleiche gelten für `int` und `char`; `bool` und `string` unterstützen nur `==` und `!=`. Die Operatorpräzedenz folgt dem C++-Standard. Implizite bool-Konvertierung findet ausschließlich in `if`/`while`-Bedingungen statt. Kontrollfluss umfasst `if-else`, `while` und Blöcke. `break` und `continue` sind nicht Teil des Sprachumfangs.

#### Funktionen und Überladung

Überladung erfolgt per exakt passender Signatur (Name, Arität, Typen inkl. `&`-Markierung); Mehrdeutigkeit erzeugt einen Fehler. Eingebaut sind `print_bool`, `print_int`, `print_char` und `print_string`. Gültige Einstiegspunkte sind `int main()` und `void main()`.

#### Klassen, Vererbung und Polymorphie

Klassen werden als `class A { public: ... }` definiert (alles public). Ein parameterloser Konstruktor wird synthetisiert, falls keiner angegeben ist. Einfachvererbung folgt dem Schema `class D : public B { ... }`. Bei abgeleiteten Klassen wird implizit der parameterlose Basiskonstruktor aufgerufen. Namensauflösung: lokal → eigene Members → geerbte Members → global. Zuweisung `Base b = d;` führt zum Slicing. Polymorphie erfolgt ausschließlich über Referenzen mit `virtual`-Methoden. `this` existiert nicht.

#### Scoping und REPL-Semantik

Variablen unterliegen define-before-use; Funktionen und Klassen erlauben define-after-use (Mehrpass beim Datei-Start). In der REPL gilt stets define-before-use: neue Variablen landen im Sitzungs-Scope, neue Funktionen und Klassen im globalen Scope.

#### Nicht unterstützt

Pointer, Casts, Arrays, Inkrement/Dekrement, Compound-Assignments, `break`/`continue`, Mehrfachvererbung, Templates, `static`, `const`, `this`, globale Variablen, Initialisierungslisten, Destruktoren sowie reine Funktionsdeklarationen.

---

### Architektur

Das System besteht aus drei Schichten: dem gemeinsamen Kern-Interpreter, dem LSP-Server (Clemens) und dem MCP-Server (Dennis). Die Verarbeitungskette lautet:

Quelle → ANTLR4-Lexer → Token-Stream → ANTLR4-Parser → AST → Resolver → Typprüfung → Interpreter

#### Interpreter

**Lexer (ANTLR4):** Tokenisiert den Quellcode unter Beibehaltung von Positionsinformationen (Zeile/Spalte) für LSP-Diagnostics. Präprozessor-Zeilen werden wie Kommentare übersprungen. Der Lexer wird aus der `.g4`-Grammatik generiert.

**Parser (ANTLR4, LL(*)):** Erzeugt einen Parse-Tree, aus dem ein handgeschriebener AST aufgebaut wird. Jeder AST-Knoten trägt Source-Positionen. Der integrierte ANTLR4-Error-Listener ermöglicht robuste Fehlerbehandlung ohne Abbruch.

**Resolver:** Verwendet eine Two-Pass-Strategie: Pass 1 sammelt alle Funktions- und Klassendeklarationen; Pass 2 löst alle Referenzen auf. Die Scope-Kette lautet: Block → Methoden-Scope → Klassen-Scope → Global-Scope → Sitzungs-Scope.

**Interpreter:** Wertet den AST rekursiv per Visitor-Pattern aus. Jeder AST-Knoten-Typ besitzt eine eigene `visit`-Methode; der Interpreter ist eine `ASTVisitor`-Implementierung. Ein Laufzeit-Stack mit Activation Records verwaltet Funktionsaufrufe. Klasseninstanzen sind `Map<String, Value>`-Objekte (feldweise Kopie). Dynamischer Dispatch erfolgt via vtable-Lookup bei Referenz-Methodenaufrufen.

**REPL:** Liest beim Start eine optionale Datei ein, führt `main()` im Sitzungs-Scope aus und hält diesen offen. Bei unvollständiger Eingabe wird ein Hilfsprompt angezeigt.

**Typprüfung und Resolver:** Da `auto` nicht zum Sprachumfang gehört, ist keine vollständige Typinferenz erforderlich. Es kommt eine deklarationsbasierte Typprüfung mit Mehrpass-Resolver zum Einsatz. Typen werden als Java-Enum bzw. Klassenhierarchie modelliert; die Typprüfung ist ein weiterer Visitor über den AST.

Jeder AST-Knoten erhält nach Bottom-up-Auswertung einen annotierten Typ. Bei binären Operatoren müssen beide Seiten identische Typen haben (Ausnahme: Zuweisung mit Slicing). Überladungsauflösung erfolgt per exaktem Match; Mehrdeutigkeit ist ein Fehler. Alle Return-Pfade einer Funktion müssen denselben Typ liefern.

Für virtuelle Methoden erhält jede Klasse zur Laufzeit eine vtable, in Java als `Map<String, Method>` realisiert. Bei Aufruf über eine Referenz wird der tatsächliche Laufzeit-Typ nachgeschlagen (dynamischer Dispatch). Nicht-virtuelle Methoden binden statisch.

#### LSP

Implementiert Language Server Protocol 3.17 über JSON-RPC mit der Java-Bibliothek `LSP4J`. Inkrementelles Parsen analysiert nur veränderte Dokumentsegmente neu; Debouncing von ca. 200 ms verhindert übermäßige Re-Analyse. Als IDE-Client dient eine VS Code Extension (`vscode-languageclient`).

| Feature | Prio | Beschreibung |
|---|---|---|
| `publishDiagnostics` | P0 | Echtzeit-Fehleranzeige (Lexer, Parser, Typ) |
| `completion` | P0 | Autovervollständigung (Variablen, Funktionen, Keywords) |
| `hover` | P0 | Typ-Information beim Hover über Bezeichner |
| `definition` | P1 | Go-to-Definition |
| `references` | P1 | Find-All-References |
| `formatting` | P2 | Code-Formatierung |
| `rename` | P2 | Rename-Refactoring über alle Referenzen |
| `codeAction` | P2 | Quick Fixes |

#### MCP

Stellt GenAI-Funktionalitäten über REST bereit und bindet Ollama als lokalen GenAI-Provider an. Empfohlene Modelle: `codellama` oder `deepseek-coder`. Da Ollama lokal läuft, entfallen API-Kosten und Datenschutzbedenken. Ein Mock-Provider implementiert dasselbe Interface für deterministische Tests.

| Endpunkt | GenAI | Funktion |
|---|---|---|
| `/complete` | Ja | Code-Vervollständigung an Cursor-Position |
| `/explain` | Ja | Natürlichsprachliche Erklärung eines Code-Fragments |
| `/refactor` | Ja | Refactoring-Vorschlag mit Begründung |
| `/detect-bugs` | Ja | Potenzielle Laufzeit- und Logikfehler erkennen |
| `/health` | Nein | Server-Status und Modell-Info |

---

## Arbeitsplan

### Übersicht (3 Monate / 12 Wochen)

| KW | Schwerpunkt | Verantw. |
|---|---|---|
| 1 | Repo (Maven/Gradle), ANTLR4 einrichten, Grammatik-Entwurf, CI/CD | C+D |
| 2–4 | ANTLR4-Lexer, ANTLR4-Parser, AST-Klassenhierarchie, Error-Recovery | C+D |
| 5–6 | Two-Pass-Resolver, Typprüfung, Semantic Checks | C+D |
| 7–8 | Tree-Walking-Interpreter, REPL, Objekt-Modell, vtable | C+D |
| 9–11 | LSP4J-Server, VS Code Extension, inkrementelles Parsen | C |
| 9–11 | MCP-Server, Ollama-Anbindung, Mocking | D |
| 12 | Benchmarks, Korrektheitstests, Doku, Walk-Through | C+D |

### Aufgabenteilung

| Aufgabe | Clemens Vogtländer | Dennis Gorpinic |
|---|---|---|
| ANTLR4-Lexer-Grammatik (.g4) | 50 % | 50 % |
| ANTLR4-Parser-Grammatik (.g4) | 50 % | 50 % |
| AST-Klassenhierarchie | 50 % | 50 % |
| Two-Pass-Resolver | 50 % | 50 % |
| Typprüfung und Overload-Auflösung | 50 % | 50 % |
| Tree-Walking-Interpreter | 50 % | 50 % |
| Objekt-Modell (Klassen, vtable, Slicing) | 50 % | 50 % |
| REPL | 50 % | 50 % |
| LSP-Server (LSP4J, alle Features) | 100 % | – |
| VS Code Extension | 100 % | – |
| MCP-Server (alle Endpunkte) | – | 100 % |
| Ollama-Anbindung + Prompt-Engineering | – | 100 % |
| Mock-Provider für Tests | 20 % | 80 % |
| Korrektheitstests + GCC-Vergleich | 50 % | 50 % |
| JMH-Benchmarks | 50 % | 50 % |
| Abschlussdokumentation + Walk-Through | 50 % | 50 % |
