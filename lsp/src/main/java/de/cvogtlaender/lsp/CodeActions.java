package de.cvogtlaender.lsp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.FieldDecl;
import de.cvogtlaender.interpreter.ast.declaration.MethodDecl;

public final class CodeActions {

  private static final Pattern MISSING = Pattern.compile("^missing '(.+)' at ");
  private static final Pattern EXTRANEOUS = Pattern.compile("^extraneous input '(.+)' expecting");
  private static final Pattern UNDECLARED = Pattern.compile("^use of undeclared identifier '(\\w+)'");
  private static final Pattern UNKNOWN_TYPE = Pattern.compile("^unknown (?:base class|type) '(\\w+)'");
  private static final Pattern NO_MEMBER = Pattern.compile("^class '(\\w+)' has no member named '(\\w+)'");
  private static final Pattern MUST_CALL = Pattern.compile("^method '(\\w+)' must be called");
  private static final int MAX_SUGGESTIONS = 3;

  private CodeActions() {
  }

  public static List<CodeAction> quickFixes(String uri, Analysis analysis, List<Diagnostic> diagnostics) {
    List<CodeAction> actions = new ArrayList<>();
    for (Diagnostic d : diagnostics) {
      if (d.getMessage() == null || !d.getMessage().isLeft()) {
        continue;
      }
      try {
        fixes(uri, analysis, d, d.getMessage().getLeft(), actions);
      } catch (RuntimeException e) {
        System.err.println("quick fix for '" + d.getMessage().getLeft() + "' failed: " + e);
      }
    }
    return actions;
  }

  private static void fixes(String uri, Analysis analysis, Diagnostic d, String message, List<CodeAction> actions) {
    SourceText text = analysis.text();
    String src = text.text();
    int start = text.offset(d.getRange().getStart());
    int end = text.offset(d.getRange().getEnd());
    Matcher m;

    if ((m = MISSING.matcher(message)).find()) {
      int at = start;
      while (at > 0 && Character.isWhitespace(src.charAt(at - 1))) {
        at--;
      }
      actions.add(action("Insert '" + m.group(1) + "'", uri, d, edit(text, at, at, m.group(1)), true));
    } else if ((m = EXTRANEOUS.matcher(message)).find()) {
      actions.add(action("Remove '" + m.group(1) + "'", uri, d, edit(text, start, end, ""), true));
    } else if ((m = UNDECLARED.matcher(message)).find()) {
      Set<String> names = new LinkedHashSet<>();
      if (analysis.hasProgram()) {
        AstNodes.Scope scope = AstNodes.scopeAt(analysis.program(), text, start);
        scope.locals().forEach(decl -> names.add(Names.of(decl)));
        if (scope.enclosingClass() != null) {
          names.addAll(memberNames(scope.enclosingClass()));
        }
        names.addAll(analysis.globals().getFunctions().keySet());
        names.addAll(analysis.globals().getClasses().keySet());
      }
      suggest(uri, d, text, start, end, m.group(1), names, actions);
    } else if ((m = UNKNOWN_TYPE.matcher(message)).find()) {
      Set<String> names = new LinkedHashSet<>(Completions.TYPES);
      if (analysis.hasProgram()) {
        names.addAll(analysis.globals().getClasses().keySet());
      }
      Matcher word = Pattern.compile("\\b" + m.group(1) + "\\b").matcher(src).region(start, end);
      if (word.find()) {
        int at = word.start();
        suggest(uri, d, text, at, at + m.group(1).length(), m.group(1), names, actions);
      }
    } else if ((m = NO_MEMBER.matcher(message)).find()) {
      ClassDecl cls = analysis.hasProgram() ? analysis.globals().getClass(m.group(1)) : null;
      String member = m.group(2);
      if (cls != null && src.startsWith(member, end - member.length())) {
        suggest(uri, d, text, end - member.length(), end, member, memberNames(cls), actions);
      }
    } else if (message.endsWith("did you mean to use '->'?")) {
      int dot = src.lastIndexOf('.', end - 1);
      if (dot >= start) {
        actions.add(action("Use '->'", uri, d, edit(text, dot, dot + 1, "->"), true));
      }
    } else if ((m = MUST_CALL.matcher(message)).find()) {
      actions.add(action("Call '" + m.group(1) + "()'", uri, d, edit(text, end, end, "()"), true));
    } else if (message.equals("no 'main' function defined")) {
      String prefix = src.isEmpty() || src.endsWith("\n") ? "" : "\n";
      String main = prefix + (src.isBlank() ? "" : "\n") + "int main() {\n  return 0;\n}\n";
      actions.add(action("Add 'int main()'", uri, d, edit(text, src.length(), src.length(), main), true));
    }
  }

  private static void suggest(String uri, Diagnostic d, SourceText text, int start, int end, String wrong,
      Set<String> candidates, List<CodeAction> actions) {
    List<String> similar = candidates.stream()
        .filter(name -> !name.equals(wrong))
        .filter(name -> distance(name, wrong) <= Math.max(1, Math.min(3, wrong.length() / 3)))
        .sorted(Comparator.comparingInt((String name) -> distance(name, wrong)).thenComparing(name -> name))
        .limit(MAX_SUGGESTIONS)
        .toList();
    for (int i = 0; i < similar.size(); i++) {
      actions.add(action("Change to '" + similar.get(i) + "'", uri, d, edit(text, start, end, similar.get(i)), i == 0));
    }
  }

  private static Set<String> memberNames(ClassDecl cls) {
    Set<String> names = new LinkedHashSet<>();
    for (ClassDecl c = cls; c != null; c = c.getParent()) {
      c.getFields().stream().map(FieldDecl::getName).forEach(names::add);
      c.getMethods().stream().map(MethodDecl::getName).forEach(names::add);
    }
    return names;
  }

  static int distance(String a, String b) {
    int[][] d = new int[a.length() + 1][b.length() + 1];
    for (int i = 0; i <= a.length(); i++) {
      d[i][0] = i;
    }
    for (int j = 0; j <= b.length(); j++) {
      d[0][j] = j;
    }
    for (int i = 1; i <= a.length(); i++) {
      for (int j = 1; j <= b.length(); j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
        if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
          d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
        }
      }
    }
    return d[a.length()][b.length()];
  }

  private static TextEdit edit(SourceText text, int start, int end, String replacement) {
    return new TextEdit(text.range(start, end), replacement);
  }

  private static CodeAction action(String title, String uri, Diagnostic d, TextEdit edit, boolean preferred) {
    CodeAction action = new CodeAction(title);
    action.setKind(CodeActionKind.QuickFix);
    action.setDiagnostics(List.of(d));
    action.setEdit(new WorkspaceEdit(Map.of(uri, List.of(edit))));
    action.setIsPreferred(preferred);
    return action;
  }
}
