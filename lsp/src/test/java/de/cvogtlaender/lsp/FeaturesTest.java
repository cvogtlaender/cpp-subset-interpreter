package de.cvogtlaender.lsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionException;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.cvogtlaender.interpreter.MiniCpp;

class FeaturesTest {

  private static final String URI = "file:///features.cpp";

  private static final String PROGRAM = """
      class Animal {
      public:
        int legs;
        Animal() { legs = 4; }
        virtual int speak(int times) { return times; }
      };

      class Dog : public Animal {
      public:
        int speak(int times) { return times * 2; }
        int twice() { return speak(2); }
      };

      int helper(Animal& a) {
        return a.speak(1) + a.legs;
      }

      int main() {
        Dog d;
        Animal* p = new Dog();
        int total = p->speak(3) + helper(d);
        delete p;
        return total;
      }
      """;

  private MiniCppLanguageServer server;
  private TextDocumentService docs;
  private String text;

  @BeforeEach
  void start() {
    server = new MiniCppLanguageServer(0);
    server.initialize(new InitializeParams()).join();
    docs = server.getTextDocumentService();
  }

  @AfterEach
  void stop() {
    server.shutdown().join();
  }

  @Test
  void programIsValid() {
    assertTrue(MiniCpp.compile(PROGRAM).diagnostics().isEmpty(), () -> MiniCpp.compile(PROGRAM).diagnostics().toString());
  }

  // Hover

  @Test
  void hoverShowsDeclarations() {
    open(PROGRAM);
    assertHover("int total", "total = p", 0, "local variable");
    assertHover("int Animal::legs", "a.legs", 2, "field");
    assertHover("Animal& a", "a.speak", 0, "parameter");
    assertHover("virtual int Dog::speak(int times)", "speak(2)", 0, "overrides `Animal::speak`");
    assertHover("virtual int Animal::speak(int times)", "speak(3)", 0, "virtual method");
    assertHover("class Dog : public Animal", "Dog d", 0, "class");
    assertHover("void print_int(int value)", null, 0, null);
  }

  @Test
  void hoverShowsExpressionTypes() {
    open(PROGRAM);
    Hover hover = docs.hover(new HoverParams(id(), pos("+ helper", 0))).join();
    assertEquals("```cpp\nint\n```", hover.getContents().getRight().getValue());
    Hover pointer = docs.hover(new HoverParams(id(), pos("new Dog", 1))).join();
    assertEquals("```cpp\nDog*\n```", pointer.getContents().getRight().getValue());
  }

  private void assertHover(String signature, String anchor, int shift, String kind) {
    if (anchor == null) {
      open("int main() { print_int(1); }");
      anchor = "print_int";
    }
    Hover hover = docs.hover(new HoverParams(id(), pos(anchor, shift))).join();
    assertNotNull(hover, anchor);
    String value = hover.getContents().getRight().getValue();
    assertTrue(value.startsWith("```cpp\n" + signature + "\n```"), value);
    if (kind != null) {
      assertTrue(value.contains(kind), value);
    }
  }

  // Navigation

  @Test
  void definitionFollowsResolvedTargets() {
    open(PROGRAM);
    assertEquals(declaration("int legs", 4), definition("a.legs", 2));
    assertEquals(declaration("int total", 4), definition("return total", 7));
    // method call through a pointer: the statically chosen method
    assertEquals(declaration("virtual int speak", 12), definition("p->speak", 3));
    // implicit method call inside a class
    assertEquals(declaration("int speak(int times) { return times * 2", 4), definition("speak(2)", 0));
    // constructor call: the declared constructor, or the class for an implicit one
    assertEquals(declaration("Animal() {", 0), definition("Animal() {", 0));
    assertEquals(declaration("class Dog", 6), definition("new Dog", 4));
    assertEquals(declaration("class Animal", 6), definition("public Animal", 7));

    open("int main() { print_int(1); }");
    assertEquals(List.of(), docs.definition(new DefinitionParams(id(), pos("print", 0))).join().getLeft());
  }

  @Test
  void referencesGroupClassesWithConstructorsAndTypes() {
    open(PROGRAM);
    List<? extends Location> refs = references("class Animal", 6, true);
    // class name, constructor, base class, parameter type, pointer type
    assertEquals(5, refs.size());
    assertEquals(4, references("class Animal", 6, false).size());
  }

  @Test
  void referencesGroupOverridingMethods() {
    open(PROGRAM);
    // Animal::speak, Dog::speak, speak(2), a.speak, p->speak
    assertEquals(5, references("p->speak", 3, true).size());
    assertEquals(5, references("int speak(int times) { return times * 2", 4, true).size());
  }

  @Test
  void renameUpdatesAllOccurrences() {
    open(PROGRAM);
    WorkspaceEdit edit = docs.rename(new RenameParams(id(), pos("int legs", 4), "limbs")).join();
    String renamed = apply(PROGRAM, edit.getChanges().get(URI));
    assertEquals(PROGRAM.replace("legs", "limbs"), renamed);
    assertTrue(MiniCpp.compile(renamed).diagnostics().isEmpty());

    edit = docs.rename(new RenameParams(id(), pos("class Animal", 6), "Pet")).join();
    renamed = apply(PROGRAM, edit.getChanges().get(URI));
    assertEquals(PROGRAM.replace("Animal", "Pet"), renamed);
  }

  @Test
  void renameRejectsBuiltinsAndKeywords() {
    open("int main() { int x = 1; print_int(x); return x; }");
    assertNull(docs.prepareRename(new PrepareRenameParams(id(), pos("print_int", 0))).join());
    assertEquals(new Range(new Position(0, 17), new Position(0, 18)),
        docs.prepareRename(new PrepareRenameParams(id(), pos("x = 1", 0))).join().getFirst());
    CompletionException e = assertThrows(CompletionException.class,
        () -> docs.rename(new RenameParams(id(), pos("x = 1", 0), "while")).join());
    assertTrue(e.getCause().getMessage().contains("not a valid identifier"));
  }

  @Test
  void documentSymbolsOutlineClassesAndFunctions() {
    open(PROGRAM);
    List<DocumentSymbol> symbols = docs.documentSymbol(new DocumentSymbolParams(id())).join().stream()
        .map(e -> e.getRight()).toList();
    assertEquals(List.of("Animal", "Dog", "helper", "main"), symbols.stream().map(DocumentSymbol::getName).toList());
    assertEquals(List.of("legs", "Animal", "speak"),
        symbols.get(0).getChildren().stream().map(DocumentSymbol::getName).toList());
  }

  // Completion

  @Test
  void completesLocalsFunctionsAndKeywords() {
    String source = "int twice(int n) { return n * 2; }\nint main() {\n  int count = 1;\n  \n  return 0;\n}\n";
    open(source);
    List<String> labels = labels(complete(new Position(3, 2)));
    assertTrue(labels.containsAll(List.of("count", "twice", "print_int", "while", "int")), labels.toString());
    assertFalse(labels.contains("n"), "parameter of another function");
    assertFalse(labels.contains("class"), "declaration keyword inside a body");
  }

  @Test
  void completesMembersWhileTheTextIsIncomplete() {
    open(PROGRAM);
    // typing 'd.' leaves the program unparsable; members come from the last good analysis
    String edited = PROGRAM.replace("  delete p;", "  d.\n  delete p;");
    change(2, edited);
    Position afterDot = position(edited, edited.indexOf("d.\n") + 2);
    List<String> labels = labels(complete(afterDot));
    assertEquals(List.of("speak", "twice", "legs"), labels);

    edited = PROGRAM.replace("  delete p;", "  p->\n  delete p;");
    change(3, edited);
    assertEquals(List.of("legs", "speak"), labels(complete(position(edited, edited.indexOf("p->\n") + 3))));
  }

  @Test
  void completesMembersOfChains() {
    String source = """
        class Node {
        public:
          int value;
          Node* next;
          Node* self() { return next; }
        };
        int main() {
          Node n;
          n.next->self()->
        }
        """;
    open(source.replace("->\n", ";\n"));
    change(2, source);
    List<String> labels = labels(complete(position(source, source.indexOf("->\n") + 2)));
    assertEquals(List.of("value", "next", "self"), labels);
  }

  // Formatting

  @Test
  void formatsCode() {
    String messy = """
        #include <iostream>
        class A{public: int x;   // the value
        virtual int get(){return x;}
        };
        class B:public A{
        public:
        int* p;int get(){ if(x>0)return -x ; else if (x==0) { return 0; } else return *p; }};
        int main(){
          B b;int& r=b.x;


          /* note */
          while(r<3) r=r+1;
          print_int(b.get()*2);
          return 0;}
        """;
    String expected = """
        #include <iostream>
        class A {
        public:
          int x; // the value
          virtual int get() {
            return x;
          }
        };

        class B : public A {
        public:
          int* p;
          int get() {
            if (x > 0)
              return -x;
            else if (x == 0) {
              return 0;
            } else
              return *p;
          }
        };

        int main() {
          B b;
          int& r = b.x;

          /* note */
          while (r < 3)
            r = r + 1;
          print_int(b.get() * 2);
          return 0;
        }
        """;
    open(messy);
    String formatted = apply(messy, format());
    assertEquals(expected, formatted);
    assertEquals(MiniCpp.compile(messy).diagnostics(), MiniCpp.compile(formatted).diagnostics());

    open(formatted);
    assertTrue(format().isEmpty(), "formatting is idempotent");
  }

  @Test
  void doesNotFormatBrokenCode() {
    open("int main() { int x = ; }");
    assertTrue(format().isEmpty());
  }

  // Code actions

  @Test
  void suggestsSimilarNames() {
    open("int main() {\n  int count = 1;\n  return cuont;\n}\n");
    List<CodeAction> actions = quickFixes();
    assertEquals("Change to 'count'", actions.get(0).getTitle());
    assertEquals("int main() {\n  int count = 1;\n  return count;\n}\n", apply(text, edits(actions.get(0))));
  }

  @Test
  void insertsMissingSemicolon() {
    open("int main() {\n  int x = 1\n  return x;\n}\n");
    List<CodeAction> actions = quickFixes();
    assertEquals("Insert ';'", actions.get(0).getTitle());
    assertEquals("int main() {\n  int x = 1;\n  return x;\n}\n", apply(text, edits(actions.get(0))));
  }

  @Test
  void fixesMemberAccessOnPointers() {
    open("class A {\npublic:\n  int v;\n};\nint main() {\n  A* a = new A;\n  return a.v + a.w;\n}\n");
    List<CodeAction> actions = quickFixes();
    assertEquals("Use '->'", actions.get(0).getTitle());
    assertTrue(apply(text, edits(actions.get(0))).contains("return a->v + a.w;"));
  }

  @Test
  void addsMissingMain() {
    open("int f() { return 1; }");
    List<CodeAction> actions = quickFixes();
    assertEquals("Add 'int main()'", actions.get(0).getTitle());
    String fixed = apply(text, edits(actions.get(0)));
    assertTrue(MiniCpp.compile(fixed).diagnostics().isEmpty(), fixed);
  }

  // Synchronization

  @Test
  void appliesIncrementalChanges() {
    open("int main() {\n  return 1;\n}\n");
    TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent(
        new Range(new Position(1, 9), new Position(1, 10)), "42");
    docs.didChange(new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(URI, 2), List.of(change)));
    Document doc = ((MiniCppTextDocumentService) docs).document(URI);
    assertEquals("int main() {\n  return 42;\n}\n", doc.text());
    assertEquals(2, doc.version());
  }

  // Helpers

  private void open(String source) {
    text = source;
    docs.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(URI, "minicpp", 1, source)));
    // as if the debounced analysis had run before the next edit
    ((MiniCppTextDocumentService) docs).document(URI).analysis();
  }

  private void change(int version, String source) {
    text = source;
    docs.didChange(new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(URI, version),
        List.of(new TextDocumentContentChangeEvent(source))));
  }

  private TextDocumentIdentifier id() {
    return new TextDocumentIdentifier(URI);
  }

  /** Position of the first occurrence of {@code anchor}, shifted by {@code shift} characters. */
  private Position pos(String anchor, int shift) {
    int offset = text.indexOf(anchor);
    assertTrue(offset >= 0, anchor);
    return position(text, offset + shift);
  }

  private static Position position(String source, int offset) {
    return new SourceText(source).position(offset);
  }

  private Range declaration(String anchor, int shift) {
    Position start = pos(anchor, shift);
    int offset = text.indexOf(anchor) + shift;
    int end = offset;
    while (Character.isJavaIdentifierPart(text.charAt(end))) {
      end++;
    }
    return new Range(start, position(text, end));
  }

  private Range definition(String anchor, int shift) {
    List<? extends Location> locations = docs.definition(new DefinitionParams(id(), pos(anchor, shift))).join()
        .getLeft();
    assertEquals(1, locations.size(), anchor);
    return locations.get(0).getRange();
  }

  private List<? extends Location> references(String anchor, int shift, boolean includeDeclaration) {
    return docs.references(new ReferenceParams(id(), pos(anchor, shift), new ReferenceContext(includeDeclaration)))
        .join();
  }

  private List<CompletionItem> complete(Position position) {
    return docs.completion(new CompletionParams(id(), position)).join().getLeft();
  }

  private static List<String> labels(List<CompletionItem> items) {
    return items.stream().map(CompletionItem::getLabel).toList();
  }

  private List<? extends TextEdit> format() {
    return docs.formatting(new DocumentFormattingParams(id(), new FormattingOptions(2, true))).join();
  }

  private List<CodeAction> quickFixes() {
    Analysis analysis = ((MiniCppTextDocumentService) docs).document(URI).analysis();
    List<Diagnostic> diagnostics = analysis.compilation().diagnostics().stream().map(Positions::toLsp).toList();
    Range all = new SourceText(text).wholeDocument();
    return docs.codeAction(new CodeActionParams(id(), all, new CodeActionContext(diagnostics))).join().stream()
        .map(e -> e.getRight()).toList();
  }

  private static List<? extends TextEdit> edits(CodeAction action) {
    return action.getEdit().getChanges().get(URI);
  }

  /** Applies non-overlapping edits to a text. */
  private static String apply(String source, List<? extends TextEdit> edits) {
    SourceText text = new SourceText(source);
    StringBuilder result = new StringBuilder(source);
    edits.stream()
        .sorted(Comparator.comparingInt((TextEdit e) -> text.offset(e.getRange().getStart())).reversed())
        .forEach(e -> result.replace(text.offset(e.getRange().getStart()), text.offset(e.getRange().getEnd()),
            e.getNewText()));
    return result.toString();
  }
}
