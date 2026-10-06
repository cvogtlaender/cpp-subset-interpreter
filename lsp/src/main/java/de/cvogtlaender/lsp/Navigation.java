package de.cvogtlaender.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

import de.cvogtlaender.interpreter.ast.AstNode;
import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.ConstructorDecl;
import de.cvogtlaender.interpreter.ast.declaration.Decl;
import de.cvogtlaender.interpreter.ast.declaration.FieldDecl;
import de.cvogtlaender.interpreter.ast.declaration.MethodDecl;

public final class Navigation {

  private static final Set<String> RESERVED = Set.of("int", "bool", "char", "string", "void", "class", "public",
      "virtual", "if", "else", "while", "return", "new", "delete", "nullptr", "true", "false");

  private Navigation() {
  }

  public static List<Location> definition(String uri, Analysis analysis, Position position) {
    SymbolIndex.Occurrence occurrence = occurrenceAt(analysis, position);
    if (occurrence == null) {
      return List.of();
    }
    Decl target = occurrence.target();
    SymbolIndex.Occurrence declaration = analysis.index().declarationOf(target);
    if (declaration == null && target instanceof ConstructorDecl k && k.getOwner() != null) {
      declaration = analysis.index().declarationOf(k.getOwner());
    }
    if (declaration == null) {
      return List.of();
    }
    return List.of(location(uri, analysis, declaration));
  }

  public static List<Location> references(String uri, Analysis analysis, Position position,
      boolean includeDeclaration) {
    SymbolIndex.Occurrence occurrence = occurrenceAt(analysis, position);
    if (occurrence == null) {
      return List.of();
    }
    return analysis.index().occurrencesOf(occurrence.symbol()).stream()
        .filter(o -> includeDeclaration || !o.declaration())
        .map(o -> location(uri, analysis, o))
        .toList();
  }

  public static List<DocumentHighlight> highlights(Analysis analysis, Position position) {
    SymbolIndex.Occurrence occurrence = occurrenceAt(analysis, position);
    if (occurrence == null) {
      return List.of();
    }
    return analysis.index().occurrencesOf(occurrence.symbol()).stream()
        .map(o -> new DocumentHighlight(analysis.text().range(o.start(), o.end()),
            o.declaration() ? DocumentHighlightKind.Write : DocumentHighlightKind.Read))
        .toList();
  }

  public static Range prepareRename(Analysis analysis, Position position) {
    SymbolIndex.Occurrence occurrence = occurrenceAt(analysis, position);
    if (occurrence == null || !renameable(analysis, occurrence.symbol())) {
      return null;
    }
    return analysis.text().range(occurrence.start(), occurrence.end());
  }

  public static WorkspaceEdit rename(String uri, Analysis analysis, Position position, String newName) {
    SymbolIndex.Occurrence occurrence = occurrenceAt(analysis, position);
    if (occurrence == null) {
      return null;
    }
    if (!renameable(analysis, occurrence.symbol())) {
      throw error("built-in functions cannot be renamed");
    }
    if (!newName.matches("[A-Za-z_][A-Za-z0-9_]*") || RESERVED.contains(newName)) {
      throw error("'" + newName + "' is not a valid identifier");
    }
    List<TextEdit> edits = new ArrayList<>();
    for (SymbolIndex.Occurrence o : analysis.index().occurrencesOf(occurrence.symbol())) {
      edits.add(new TextEdit(analysis.text().range(o.start(), o.end()), newName));
    }
    return new WorkspaceEdit(Map.of(uri, edits));
  }

  private static boolean renameable(Analysis analysis, Decl symbol) {
    return analysis.index().declarationOf(symbol) != null;
  }

  public static List<DocumentSymbol> documentSymbols(Analysis analysis) {
    if (analysis.index() == null) {
      return List.of();
    }
    List<DocumentSymbol> symbols = new ArrayList<>();
    List<AstNode> declarations = AstNodes.children(analysis.program());
    for (AstNode node : declarations) {
      DocumentSymbol symbol = symbol(analysis, (Decl) node);
      if (node instanceof ClassDecl c) {
        List<DocumentSymbol> members = new ArrayList<>();
        for (AstNode member : AstNodes.children(c)) {
          members.add(symbol(analysis, (Decl) member));
        }
        symbol.setChildren(members);
      }
      symbols.add(symbol);
    }
    return symbols;
  }

  private static DocumentSymbol symbol(Analysis analysis, Decl decl) {
    SourceText text = analysis.text();
    Range range = text.range(text.startOffset(decl), text.endOffset(decl));
    SymbolIndex.Occurrence name = analysis.index().declarationOf(decl);
    Range selection = name == null ? range : text.range(name.start(), name.end());
    SymbolKind kind = switch (decl) {
      case ClassDecl c -> SymbolKind.Class;
      case ConstructorDecl k -> SymbolKind.Constructor;
      case MethodDecl m -> SymbolKind.Method;
      case FieldDecl f -> SymbolKind.Field;
      default -> SymbolKind.Function;
    };
    DocumentSymbol symbol = new DocumentSymbol(Names.of(decl), kind, range, selection);
    symbol.setDetail(Names.detail(decl));
    return symbol;
  }

  private static SymbolIndex.Occurrence occurrenceAt(Analysis analysis, Position position) {
    return analysis.index() == null ? null : analysis.index().at(analysis.text().offset(position));
  }

  private static Location location(String uri, Analysis analysis, SymbolIndex.Occurrence o) {
    return new Location(uri, analysis.text().range(o.start(), o.end()));
  }

  private static ResponseErrorException error(String message) {
    return new ResponseErrorException(new ResponseError(ResponseErrorCode.InvalidParams, message, null));
  }
}
