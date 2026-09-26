package de.cvogtlaender.interpreter.runtime;

/** Formatting of runtime values for output and the REPL. */
public final class Values {

  private Values() {
  }

  /** Text as produced by the print_* built-ins. */
  public static String format(Object value) {
    return switch (value) {
      case null -> "void";
      case Boolean b -> b ? "true" : "false";
      default -> value.toString();
    };
  }

  /** Text as shown by the REPL, with literal syntax for chars and strings. */
  public static String display(Object value) {
    return switch (value) {
      case Character c -> "'" + escape(String.valueOf(c)) + "'";
      case String s -> "\"" + escape(s) + "\"";
      default -> format(value);
    };
  }

  private static String escape(String s) {
    StringBuilder out = new StringBuilder();
    for (char c : s.toCharArray()) {
      out.append(switch (c) {
        case '\n' -> "\\n";
        case '\t' -> "\\t";
        case '\r' -> "\\r";
        case '\b' -> "\\b";
        case '\0' -> "\\0";
        case '\\' -> "\\\\";
        case '"' -> "\\\"";
        case '\'' -> "\\'";
        default -> String.valueOf(c);
      });
    }
    return out.toString();
  }
}
