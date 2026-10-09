package de.cvogtlaender.interpreter.runtime;

public final class Cell {

  public Object value;
  private boolean heap;
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

  public void kill() {
    dead = true;
    if (value instanceof ObjectValue o) {
      for (Cell field : o.getFields().values()) {
        field.kill();
      }
    }
  }
}
