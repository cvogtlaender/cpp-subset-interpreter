package de.cvogtlaender.interpreter.runtime;

/**
 * A pointer value: the cell it points to, or {@code null} for 'nullptr'.
 * Pointers are equal if they point to the same cell.
 */
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
