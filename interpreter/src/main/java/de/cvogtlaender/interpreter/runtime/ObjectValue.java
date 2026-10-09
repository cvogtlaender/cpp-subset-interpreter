package de.cvogtlaender.interpreter.runtime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.FieldDecl;
import de.cvogtlaender.interpreter.semantic.GlobalScope;

public final class ObjectValue {

  private final ClassDecl cls;
  private final Map<String, Cell> fields = new LinkedHashMap<>();
  private ClassDecl dynamicClass;

  public ObjectValue(ClassDecl cls) {
    this.cls = cls;
    this.dynamicClass = cls;
  }

  public ClassDecl getCls() {
    return cls;
  }

  public ClassDecl getDynamicClass() {
    return dynamicClass;
  }

  public void setDynamicClass(ClassDecl dynamicClass) {
    this.dynamicClass = dynamicClass;
  }

  public Map<String, Cell> getFields() {
    return fields;
  }

  public Cell field(String name) {
    return fields.get(name);
  }

  public ObjectValue copy() {
    ObjectValue result = new ObjectValue(cls);
    for (Map.Entry<String, Cell> e : fields.entrySet()) {
      result.fields.put(e.getKey(), new Cell(copyValue(e.getValue().value)));
    }
    return result;
  }

  public ObjectValue sliceTo(ClassDecl target) {
    ObjectValue result = new ObjectValue(target);
    for (FieldDecl f : GlobalScope.allFields(target)) {
      result.fields.put(f.getName(), new Cell(copyValue(fields.get(f.getName()).value)));
    }
    return result;
  }

  public static Object copyValue(Object value) {
    return value instanceof ObjectValue o ? o.copy() : value;
  }

  @Override
  public String toString() {
    return cls.getClassName() + "{" + fields.entrySet().stream()
        .map(e -> e.getKey() + " = " + Values.format(e.getValue().value))
        .collect(Collectors.joining(", ")) + "}";
  }
}
