package de.cvogtlaender.interpreter.runtime;

public record Pointer(Cell target) {

  public static final Pointer NULL = new Pointer(null);

  public boolean isNull() {
    return target == null;
  }

  @Override
  public String toString() {
    return target == null ? "nullptr" : String.format("0x%08x", System.identityHashCode(target));
  }
}
