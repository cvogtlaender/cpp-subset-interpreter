package de.cvogtlaender.interpreter.runtime;

public final class Values {

  private Values() {
  }

  public static String format(Object value) {
    return switch (value) {
      case null -> "void";
      case Boolean b -> b ? "true" : "false";
      default -> value.toString();
    };
  }

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
