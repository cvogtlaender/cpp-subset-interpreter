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

Unterstützte Basistypen sind `bool`, `int`, `char`, `string` und `void`. Escape-Sequenzen mit `\` sind erlaubt. Variablen werden per `T x;` oder `T x = expr;` deklariert. C++-Referenzen werden als `T& x = expr;` (Variable) bzw. `T& p` (Parameter) unterstützt. Referenz-Initialisierung ist obligatorisch; Zuweisung schreibt in das referenzierte Ziel (keine Neubindung). Referenz-Felder, `&`-Rückgaben und globale Variablen sind nicht erlaubt. Zeiger werden als `T* p` unterstützt, auch mehrstufig (`T**`) und als Variablen, Parameter (auch `T*&`), Felder und Rückgabewerte: Adressoperator `&x`, Dereferenzierung `*p` (lesend und schreibend), Memberzugriff `p->m` bzw. `p->m()`, das Literal `nullptr`, Heap-Objekte per `new T` bzw. `new T(args)` und Freigabe per `delete p;`.

#### Ausdrücke und Kontrollfluss

Arithmetische Operatoren (`+ - * / %`, unär `+ -`) sind auf `int` beschränkt. Vergleiche gelten für `int` und `char`; `bool` und `string` unterstützen nur `==` und `!=`. Die Operatorpräzedenz folgt dem C++-Standard. Implizite bool-Konvertierung findet ausschließlich in `if`/`while`-Bedingungen statt. Kontrollfluss umfasst `if-else`, `while` und Blöcke. `break` und `continue` sind nicht Teil des Sprachumfangs.

#### Funktionen und Überladung

Überladung erfolgt per exakt passender Signatur (Name, Arität, Typen inkl. `&`-Markierung); Mehrdeutigkeit erzeugt einen Fehler. Eingebaut sind `print_bool`, `print_int`, `print_char` und `print_string`. Gültige Einstiegspunkte sind `int main()` und `void main()`.

#### Klassen, Vererbung und Polymorphie

Klassen werden als `class A { public: ... }` definiert (alles public). Ein parameterloser Konstruktor wird synthetisiert, falls keiner angegeben ist. Einfachvererbung folgt dem Schema `class D : public B { ... }`. Bei abgeleiteten Klassen wird implizit der parameterlose Basiskonstruktor aufgerufen. Namensauflösung: lokal → eigene Members → geerbte Members → global. Zuweisung `Base b = d;` führt zum Slicing. Polymorphie erfolgt über Referenzen und Zeiger mit `virtual`-Methoden. `this` existiert nicht.

#### Scoping und REPL-Semantik

Variablen unterliegen define-before-use; Funktionen und Klassen erlauben define-after-use (Mehrpass beim Datei-Start). In der REPL gilt stets define-before-use: neue Variablen landen im Sitzungs-Scope, neue Funktionen und Klassen im globalen Scope.

#### Nicht unterstützt

Zeigerarithmetik, `void*`, Casts, Arrays, Inkrement/Dekrement, Compound-Assignments, `break`/`continue`, Mehrfachvererbung, Templates, `static`, `const`, `this`, globale Variablen, Initialisierungslisten, Destruktoren sowie reine Funktionsdeklarationen.

---

### Architektur

Das System besteht aus drei Schichten: dem gemeinsamen Kern-Interpreter, dem LSP-Server (Clemens) und dem MCP-Server (Dennis). Die Verarbeitungskette lautet:

Quelle → ANTLR4-Lexer → Token-Stream → ANTLR4-Parser → AST → Resolver → Typprüfung → Interpreter

#### Interpreter

**Lexer (ANTLR4):** Tokenisiert den Quellcode unter Beibehaltung von Positionsinformationen (Zeile/Spalte) für LSP-Diagnostics. Präprozessor-Zeilen werden wie Kommentare übersprungen. Der Lexer wird aus der `.g4`-Grammatik generiert.

**Parser (ANTLR4, LL(*)):** Erzeugt einen Parse-Tree, aus dem ein handgeschriebener AST aufgebaut wird. Jeder AST-Knoten trägt Source-Positionen. Der integrierte ANTLR4-Error-Listener ermöglicht robuste Fehlerbehandlung ohne Abbruch.

**Resolver:** Verwendet eine Two-Pass-Strategie: Pass 1 sammelt alle Funktions- und Klassendeklarationen; Pass 2 löst alle Referenzen auf. Die Scope-Kette lautet: Block → Methoden-Scope → Klassen-Scope → Global-Scope → Sitzungs-Scope.

**Interpreter:** Wertet den AST rekursiv per Visitor-Pattern aus. Jeder AST-Knoten-Typ besitzt eine eigene `visit`-Methode; der Interpreter ist eine `ASTVisitor`-Implementierung. Ein Laufzeit-Stack mit Activation Records verwaltet Funktionsaufrufe. Klasseninstanzen sind `Map<String, Value>`-Objekte (feldweise Kopie). Dynamischer Dispatch erfolgt via vtable-Lookup bei Methodenaufrufen über Referenzen und Zeiger.

**REPL:** Liest beim Start eine optionale Datei ein, führt `main()` im Sitzungs-Scope aus und hält diesen offen. Bei unvollständiger Eingabe wird ein Hilfsprompt angezeigt.

**Typprüfung und Resolver:** Da `auto` nicht zum Sprachumfang gehört, ist keine vollständige Typinferenz erforderlich. Es kommt eine deklarationsbasierte Typprüfung mit Mehrpass-Resolver zum Einsatz. Typen werden als Java-Enum bzw. Klassenhierarchie modelliert; die Typprüfung ist ein weiterer Visitor über den AST.

Jeder AST-Knoten erhält nach Bottom-up-Auswertung einen annotierten Typ. Bei binären Operatoren müssen beide Seiten identische Typen haben (Ausnahme: Zuweisung mit Slicing). Überladungsauflösung erfolgt per exaktem Match; Mehrdeutigkeit ist ein Fehler. Alle Return-Pfade einer Funktion müssen denselben Typ liefern.

Für virtuelle Methoden erhält jede Klasse zur Laufzeit eine vtable, in Java als `Map<String, Method>` realisiert. Bei Aufruf über eine Referenz oder einen Zeiger wird der tatsächliche Laufzeit-Typ nachgeschlagen (dynamischer Dispatch). Nicht-virtuelle Methoden binden statisch.

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

## Nutzung

Voraussetzung ist ein JDK 21 oder neuer.

```sh
./gradlew build                        # baut alle Module und führt alle Tests aus
./gradlew :interpreter:installDist     # erzeugt interpreter/build/install/minicpp/bin/minicpp
```

| Befehl | Wirkung |
|---|---|
| `minicpp run datei.cpp` (oder `minicpp datei.cpp`) | Programm ausführen; der Exit-Code ist der Rückgabewert von `main` |
| `minicpp check datei.cpp` | nur Syntax-, Namens- und Typprüfung |
| `minicpp repl [datei.cpp]` | REPL, optional mit vorher geladener Datei |
| `minicpp ast datei.cpp` | AST ausgeben |
| `minicpp to-cpp datei.cpp` | nach Standard-C++ übersetzen (für den GCC-Vergleich) |

Ohne Installation: `./gradlew :interpreter:run --args="run examples/features.cpp"` bzw. `--args="repl"`.

Fehlermeldungen haben die Form `datei.cpp:3:7: error: use of undeclared identifier 'y'` (Zeile:Spalte, beide 1-basiert).

### REPL

```
minicpp> int x = 6;
minicpp> int sq(int n) {
     ...>   return n * n;
     ...> }
minicpp> sq(x) + 1
37
```

Eine Eingabe darf Klassen, Funktionen und Anweisungen enthalten; ein abschließender Ausdruck ohne `;` wird ausgewertet und ausgegeben. Unvollständige Eingaben werden mit dem Hilfsprompt `...>` fortgesetzt (`:cancel` verwirft sie). Befehle: `:vars`, `:functions`, `:classes`, `:load <datei>`, `:reset`, `:help`, `:quit`.

---

## Umsetzung

### Kern-Interpreter (`interpreter/`)

| Paket | Inhalt |
|---|---|
| `MiniCpp` | Fassade für die ganze Pipeline: `parseProgram`, `compile`, `run`, `execute` |
| `visitor/ASTBuildVisitor` | Parse-Tree → AST, jeder Knoten mit Quellbereich (Zeile 1-basiert, Spalte 0-basiert) |
| `visitor/ASTResolveVisitor` | Two-Pass-Resolver: Pass 1 sammelt Klassen/Funktionen, verknüpft Basisklassen, prüft Member; Pass 2 bindet Bezeichner (lokal → eigene Member → geerbte Member → global) |
| `visitor/TypeCheckVisitor` | Typprüfung, Überladungsauflösung, L-Wert-Prüfung, Return-Pfade, Overrides/`virtual`, Einstiegspunkt |
| `runtime/Interpreter` | Tree-Walking-Interpreter mit Activation Records, `Cell`s für Referenzen und Zeiger (mit Lebensdauerprüfung), Objekten mit Wertsemantik, vtables |
| `repl/Repl` | REPL mit Sitzungs-Scope |
| `cpp/CppExporter` | Übersetzung nach Standard-C++ |
| `diagnostic/Diagnostic` | Fehler mit Phase (`SYNTAX`, `RESOLVE`, `TYPE`, `RUNTIME`) und Quellbereich |

Präzisierungen der Sprache, wo die Anforderungen offen waren:

- `print_*` gibt den Wert gefolgt von einem Zeilenumbruch aus; `bool` erscheint als `true`/`false`.
- Variablen und Felder ohne Initialisierer erhalten Standardwerte: `0`, `false`, `'\0'`, `""`; Objekte werden mit dem parameterlosen Konstruktor erzeugt.
- `T x(a, b);` ist für Klassentypen eine Kurzform von `T x = T(a, b);`.
- Überladungsauflösung: zuerst exakte Treffer (inkl. `&`, Referenzparameter brauchen L-Werte); nur wenn keiner existiert, werden die Konvertierungen Derived→Base, `D*`→`B*` und `nullptr`→`T*` berücksichtigt (nicht für Referenzparameter wie `B*&`). Mehr als ein bester Kandidat ist ein Fehler.
- Wie in C++ verdecken Member einer abgeleiteten Klasse gleichnamige Member der Basisklasse; Felder dürfen in abgeleiteten Klassen nicht erneut deklariert werden.
- Methoden, die eine virtuelle Methode überschreiben, sind selbst virtuell. Während der Konstruktor einer Basisklasse läuft, rufen virtuelle Aufrufe deren Version auf (wie in C++).
- Zuweisungen an Objekte kopieren feldweise und nur den Teil des statischen Zieltyps (Slicing, auch über Referenzen).
- Zeiger: `==`/`!=` vergleichen Zeiger kompatiblen Typs (auch mit `nullptr`); es gibt keine Zeigerarithmetik und keine `<`-Vergleiche. Wie andere Werte sind Zeiger nur in `if`/`while`-Bedingungen implizit `bool` (`!p` ist ein Fehler, stattdessen `p == nullptr`). Uninitialisierte Zeiger sind `nullptr`, `new T` ohne Argumente erzeugt den Standardwert. `new`, `delete` und `nullptr` sind Schlüsselwörter.
- Wie in C++ ist die Anweisung `a * b;` eine Deklaration (Zeiger `b` vom Typ `a*`), keine Multiplikation.
- Was in C++ undefiniertes Verhalten wäre, ist ein Laufzeitfehler: Dereferenzieren von `nullptr`, hängende Zeiger und Referenzen auf Variablen, deren Scope beendet ist, Zugriff auf gelöschte Heap-Objekte, doppeltes `delete` und `delete` auf nicht per `new` erzeugte Objekte. Nicht freigegebener Speicher ist kein Fehler.
- Ganzzahlarithmetik läuft im Zweierkomplement über; Division und Modulo durch 0 sind Laufzeitfehler, ebenso mehr als 100 000 verschachtelte Aufrufe.
- Operanden und Argumente werden von links nach rechts ausgewertet. `int main()` ohne `return` liefert 0.
- REPL: Eine Eingabe wird als Ganzes geprüft und bei Fehlern vollständig verworfen. Sitzungsvariablen sind in Funktionen nicht sichtbar (es gibt keine globalen Variablen), dürfen aber neu deklariert werden. Beim Laden einer Datei läuft `main()` im Sitzungs-Scope, seine lokalen Variablen bleiben danach verfügbar.

### LSP-Server (`lsp/`, `vscode/`)

`lsp/` ist der Server (LSP4J, JSON-RPC über stdin/stdout), `vscode/` der Client (`vscode-languageclient`). Alle Features der Tabelle oben sind umgesetzt, dazu `documentHighlight` und `documentSymbol` (Outline):

| Feature | Verhalten |
|---|---|
| Synchronisation | inkrementell (Bereichsänderungen), Re-Analyse 200 ms nach der letzten Änderung; Anfragen analysieren sofort, falls die entprellte Analyse noch aussteht |
| `publishDiagnostics` | Fehler aller Phasen (Syntax, Namen, Typen) |
| `hover` | Deklaration als C++-Signatur mit Art (`local variable`, `virtual method`, `overrides B::f` …); an Operatoren und Literalen der statische Typ des Ausdrucks |
| `completion` | Variablen und Parameter im Gültigkeitsbereich, Members der eigenen Klasse, Funktionen, Klassen, Keywords, Typen; nach `.`/`->` die Members (inkl. geerbter) des Objekts, auch über Ketten wie `a.b->c().`. Da unvollständiger Code (`a.`) nicht parst, stammen die Symbole aus der letzten parsebaren Version |
| `definition` | gewählte Überladung, statisch gebundene Methode, Konstruktor bei `A(…)`/`new A` (bei implizitem Konstruktor die Klasse) |
| `references`, `documentHighlight`, `rename` | Klassen samt Konstruktornamen und Typverwendungen, überschreibende virtuelle Methoden gemeinsam mit der Basismethode; Built-ins und Keywords sind nicht umbenennbar |
| `formatting` | auf Basis des Parse-Trees: K&R-Klammern, `public:` auf Klassenebene, Rümpfe ohne Klammern eingerückt in eigener Zeile, `T* p`, Leerzeichen um binäre Operatoren; Kommentare und einzelne Leerzeilen bleiben erhalten. Code mit Syntaxfehlern wird nicht formatiert |
| `codeAction` | Quick Fixes: fehlendes Token einfügen, überzähliges entfernen, ähnlich geschriebenen Namen vorschlagen (Bezeichner, Typen, Members), `.` → `->`, `()` an Methodennamen, leeres `main` ergänzen |

| Klasse | Inhalt |
|---|---|
| `MiniCppLanguageServerMain` | Start über stdio |
| `MiniCppLanguageServer` | Lebenszyklus (`initialize`, `shutdown`, `exit`) und Capabilities |
| `MiniCppTextDocumentService` | Dokument-Synchronisation, entprellte Diagnosen, Verteilung der Anfragen |
| `Document`, `Analysis` | offenes Dokument; Analyse einer Version (Tokens, `Compilation`, Symbolindex, letzte parsebare Version) |
| `SymbolIndex` | jeder Bezeichner mit der Deklaration, auf die er verweist (aus aufgelöstem AST und Token-Strom) |
| `AstNodes`, `Names` | AST-Traversierung, Gültigkeitsbereich an einer Position; Signaturen und Beschreibungen |
| `Hovers`, `Completions`, `Navigation`, `CodeFormatter`, `CodeActions` | die Features |
| `SourceText`, `Tokens`, `Positions` | Umrechnung Offsets ↔ Positionen (Interpreter 1-basiert, LSP 0-basiert), Token-Suche, Diagnosen |

```sh
./gradlew :lsp:installDist          # erzeugt lsp/build/install/minicpp-lsp/bin/minicpp-lsp
cd vscode && npm install && npm run compile
code vscode                         # dann F5: startet VS Code mit der Extension und öffnet examples/

cd vscode && npm run package        # erzeugt vscode/minicpp-1.0.0.vsix (Server enthalten, benötigt Java 21+)
code --install-extension vscode/minicpp-1.0.0.vsix
```

Das `.vsix` bündelt den Server aus `lsp/build/install` (vorher `./gradlew :lsp:installDist`) und startet ihn direkt mit `java`; das Java-Programm lässt sich mit `minicpp.java.path` festlegen, sonst gelten `JAVA_HOME` und der `PATH`.

Die Extension registriert die Sprache `minicpp` für `*.mcpp`; in `examples/` ordnet `.vscode/settings.json` auch `*.cpp` MiniC++ zu. Der Serverpfad lässt sich mit der Einstellung `minicpp.server.path` überschreiben, `minicpp.trace.server` protokolliert die JSON-RPC-Nachrichten.

### MCP-Server (`mcp/`)

REST-Server gemäß der Endpunkt-Tabelle oben, mit Ollama als Provider und einem `MockProvider` für Tests. Die Antworten werden mit dem echten Compiler abgesichert: Compiler-Diagnosen fließen in die Prompts ein, `/detect-bugs` liefert sie zusätzlich strukturiert, und jeder Refactoring-Vorschlag wird vor der Rückgabe kompiliert (`"compiles": true/false`).

```sh
ollama pull codellama
./gradlew :mcp:run                                   # http://127.0.0.1:8080, Provider Ollama
./gradlew :mcp:run --args="--provider mock"          # ohne Ollama
./gradlew :mcp:run --args="--model deepseek-coder --port 9000"

curl -s localhost:8080/health
curl -s localhost:8080/explain -d '{"code": "int main() { return 0; }"}'
curl -s localhost:8080/complete -d '{"code": "int main() {\n\n}", "line": 2, "column": 0}'
curl -s localhost:8080/refactor -d '{"code": "...", "instruction": "extract a function"}'
curl -s localhost:8080/detect-bugs -d '{"code": "..."}'
```

Optionen (auch als Umgebungsvariablen): `--host` (`MCP_HOST`, Standard `127.0.0.1`), `--port` (`MCP_PORT`, 8080), `--provider` (`MCP_PROVIDER`, `ollama`|`mock`), `--ollama-url` (`OLLAMA_URL`), `--model` (`OLLAMA_MODEL`, `codellama`), `--timeout` (`OLLAMA_TIMEOUT`, 120 s). Fehler werden als `{"error": ...}` mit HTTP 400 (ungültige Anfrage), 405, 413 oder 502 (Provider nicht erreichbar) gemeldet.

### Evaluation

- **Korrektheit:** `./gradlew test` führt die Tests beider Module aus: Beispielprogramme mit erwarteter Ausgabe (`interpreter/src/test/resources/programs`), über 80 Fehlerfälle aller Phasen, Parser-, REPL-, Exporter- und MCP-Tests.
- **GCC-Vergleich:** `scripts/compare-gcc.sh` führt jedes Beispielprogramm sowohl im Interpreter als auch – über `minicpp to-cpp` übersetzt – mit `g++ -std=c++17 -fwrapv` aus und vergleicht Ausgaben und Exit-Codes. Der Exporter überbrückt die bewussten Unterschiede (define-after-use, Standardwerte, `string`-Literale, `void main`, virtuelle Destruktoren für Basisklassen). Die CI (`.github/workflows/ci.yml`) führt den Vergleich bei jedem Push aus.
- **Performance/Skalierbarkeit:** `./gradlew :benchmark:jmh` (eigene JMH-Argumente: `-Pjmh="Fib -f 2"`; Ergebnisse in `benchmark/build/jmh-result.json`). Die Benchmarks messen rekursive Aufrufe, Schleifen, virtuellen Dispatch und Objektkopien mit wachsender Größe sowie Parsen und Prüfen generierter Programme mit 10–1000 Klassen.

### Hinweise für den LSP-Server

`MiniCpp.compile(text)` liefert AST, globale Symbole und Diagnosen mit Quellbereichen. Nach erfolgreicher Prüfung sind im AST aufgelöst: `VarExpr.getKind()/getResolvedDecl()`, `CallExpr.getTarget()` (gewählte Überladung), `MemberAccessExpr.getResolvedField()` (`isArrow()` für `->`), `NewExpr.getConstructor()`, `Expr.getInferredType()` (für Hover). Bei Syntaxfehlern wird derzeit kein AST erzeugt.

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
