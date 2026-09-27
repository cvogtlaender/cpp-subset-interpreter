package de.cvogtlaender.lsp;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;

import de.cvogtlaender.interpreter.ast.declaration.Decl;
import de.cvogtlaender.interpreter.ast.declaration.MethodDecl;
import de.cvogtlaender.interpreter.ast.expression.Expr;

/**
 * Hover: the declaration of an identifier with its type and kind, or the
 * static type of the innermost expression elsewhere (operators, literals).
 */
public final class Hovers {

  private Hovers() {
  }

  public static Hover hover(Analysis analysis, Position position) {
    if (analysis.index() == null) {
      return null;
    }
    SourceText text = analysis.text();
    int offset = text.offset(position);

    SymbolIndex.Occurrence occurrence = analysis.index().at(offset);
    if (occurrence != null) {
      return new Hover(markdown(describe(occurrence.target())), text.range(occurrence.start(), occurrence.end()));
    }

    Expr expr = AstNodes.innermostExpr(analysis.program(), text, offset);
    if (expr == null || expr.getInferredType() == null) {
      return null;
    }
    return new Hover(markdown(code(expr.getInferredType().getName())),
        text.range(text.startOffset(expr), text.endOffset(expr)));
  }

  static String describe(Decl decl) {
    StringBuilder result = new StringBuilder(code(Names.signature(decl)));
    result.append("\n\n*").append(Names.kind(decl)).append('*');
    if (decl instanceof MethodDecl m) {
      MethodDecl base = Names.overridden(m);
      if (base != null) {
        result.append(", overrides `").append(base.getOwner().getClassName()).append("::")
            .append(base.getName()).append('`');
      }
    }
    return result.toString();
  }

  private static String code(String source) {
    return "```cpp\n" + source + "\n```";
  }

  private static MarkupContent markdown(String value) {
    return new MarkupContent(MarkupKind.MARKDOWN, value);
  }
}
