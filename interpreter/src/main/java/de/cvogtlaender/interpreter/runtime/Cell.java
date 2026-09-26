package de.cvogtlaender.interpreter.runtime;

/**
 * A storage location. Variables, parameters and fields each own a cell; a
 * reference is simply a second name for an existing cell, a {@link Pointer}
 * holds one as its target.
 *
 * Values are {@link Integer}, {@link Boolean}, {@link Character},
 * {@link String}, {@link Pointer} or {@link ObjectValue}.
 */
public final class Cell {

  public Object value;
  // allocated by 'new' (and so may be deleted)
  private boolean heap;
  // the variable went out of scope or the heap object was deleted
  private boolean dead;

  public Cell(Object value) {
    this.value = value;
  }

  public static Cell heap(Object value) {
    Cell cell = new Cell(value);
    cell.heap = true;
    return cell;
  }

  public boolean isHeap() {
    return heap;
  }

  public boolean isDead() {
    return dead;
  }

  /** Ends the lifetime of this cell and of the fields of the object in it. */
  public void kill() {
    dead = true;
    if (value instanceof ObjectValue o) {
      for (Cell field : o.getFields().values()) {
        field.kill();
      }
    }
  }
}
