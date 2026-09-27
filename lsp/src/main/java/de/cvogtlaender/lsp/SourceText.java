package de.cvogtlaender.lsp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import de.cvogtlaender.interpreter.ast.AstNode;

/**
 * A document text with a line index, converting between character offsets,
 * LSP positions (0-based lines) and interpreter positions (1-based lines).
 */
public final class SourceText {

  private final String text;
  private final int[] lineStarts;

  public SourceText(String text) {
    this.text = text;
    List<Integer> starts = new ArrayList<>();
    starts.add(0);
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        starts.add(i + 1);
      }
    }
    lineStarts = starts.stream().mapToInt(Integer::intValue).toArray();
  }

  public String text() {
    return text;
  }

  public int length() {
    return text.length();
  }

  /** Offset of an LSP position, clamped to the document and to its line. */
  public int offset(Position position) {
    return offset(position.getLine() + 1, position.getCharacter());
  }

  /** Offset of an interpreter position (1-based line, 0-based column), clamped. */
  public int offset(int line, int column) {
    if (line < 1) {
      return 0;
    }
    if (line > lineStarts.length) {
      return text.length();
    }
    int start = lineStarts[line - 1];
    int end = line < lineStarts.length ? lineStarts[line] - 1 : text.length();
    return Math.min(start + Math.max(0, column), end);
  }

  public int startOffset(AstNode node) {
    return offset(node.getLine(), node.getColumn());
  }

  public int endOffset(AstNode node) {
    return offset(node.getEndLine(), node.getEndColumn());
  }

  public Position position(int offset) {
    offset = Math.max(0, Math.min(offset, text.length()));
    int low = 0;
    int high = lineStarts.length - 1;
    while (low < high) {
      int mid = (low + high + 1) >>> 1;
      if (lineStarts[mid] <= offset) {
        low = mid;
      } else {
        high = mid - 1;
      }
    }
    return new Position(low, offset - lineStarts[low]);
  }

  public Range range(int start, int end) {
    return new Range(position(start), position(end));
  }

  public Range wholeDocument() {
    return range(0, text.length());
  }
}
