package de.cvogtlaender.interpreter.semantic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.ConstructorDecl;
import de.cvogtlaender.interpreter.ast.declaration.Decl;
import de.cvogtlaender.interpreter.ast.declaration.FieldDecl;
import de.cvogtlaender.interpreter.ast.declaration.FunctionDecl;
import de.cvogtlaender.interpreter.ast.declaration.MethodDecl;
import de.cvogtlaender.interpreter.ast.declaration.ParameterDecl;
import de.cvogtlaender.interpreter.ast.type.Type;

public class GlobalScope {

  public static final List<String> BUILTINS = List.of("print_bool", "print_int", "print_char", "print_string");

  private final Map<String, ClassDecl> classes = new LinkedHashMap<>();
  private final Map<String, List<FunctionDecl>> functions = new LinkedHashMap<>();
  private final Map<String, Decl> session = new LinkedHashMap<>();

  public GlobalScope() {
    addBuiltin("print_bool", Types.BOOL);
    addBuiltin("print_int", Types.INT);
    addBuiltin("print_char", Types.CHAR);
    addBuiltin("print_string", Types.STRING);
  }

  private void addBuiltin(String name, Type parameterType) {
    List<ParameterDecl> params = new ArrayList<>();
    params.add(new ParameterDecl(parameterType, "value"));
    functions.computeIfAbsent(name, k -> new ArrayList<>()).add(FunctionDecl.builtin(Types.VOID, name, params));
  }

  public Map<String, ClassDecl> getClasses() {
    return classes;
  }

  public Map<String, List<FunctionDecl>> getFunctions() {
    return functions;
  }

  public Map<String, Decl> getSession() {
    return session;
  }

  public ClassDecl getClass(String name) {
    return classes.get(name);
  }

  public ClassDecl classOf(Type type) {
    Type stripped = Types.strip(type);
    return Types.isClass(stripped) ? classes.get(stripped.getName()) : null;
  }

  public static ClassDecl findMemberOwner(ClassDecl cls, String name) {
    for (ClassDecl c = cls; c != null; c = c.getParent()) {
      for (FieldDecl f : c.getFields()) {
        if (f.getName().equals(name)) {
          return c;
        }
      }
      for (MethodDecl m : c.getMethods()) {
        if (m.getName().equals(name)) {
          return c;
        }
      }
    }
    return null;
  }

  public static FieldDecl findField(ClassDecl cls, String name) {
    for (ClassDecl c = cls; c != null; c = c.getParent()) {
      for (FieldDecl f : c.getFields()) {
        if (f.getName().equals(name)) {
          return f;
        }
      }
    }
    return null;
  }

  public static List<MethodDecl> methodsNamed(ClassDecl cls, String name) {
    List<MethodDecl> result = new ArrayList<>();
    for (MethodDecl m : cls.getMethods()) {
      if (m.getName().equals(name)) {
        result.add(m);
      }
    }
    return result;
  }

  public static ConstructorDecl defaultConstructor(ClassDecl cls) {
    for (ConstructorDecl c : cls.getConstructors()) {
      if (c.getParameters().isEmpty()) {
        return c;
      }
    }
    return null;
  }

  public static List<FieldDecl> allFields(ClassDecl cls) {
    List<ClassDecl> chain = new ArrayList<>();
    for (ClassDecl c = cls; c != null; c = c.getParent()) {
      chain.add(0, c);
    }
    List<FieldDecl> fields = new ArrayList<>();
    for (ClassDecl c : chain) {
      fields.addAll(c.getFields());
    }
    return fields;
  }

  public Snapshot snapshot() {
    Map<String, List<FunctionDecl>> functionsCopy = new LinkedHashMap<>();
    functions.forEach((k, v) -> functionsCopy.put(k, new ArrayList<>(v)));
    return new Snapshot(new LinkedHashMap<>(classes), functionsCopy, new LinkedHashMap<>(session));
  }

  public void restore(Snapshot snapshot) {
    classes.clear();
    classes.putAll(snapshot.classes);
    functions.clear();
    functions.putAll(snapshot.functions);
    session.clear();
    session.putAll(snapshot.session);
  }

  public record Snapshot(Map<String, ClassDecl> classes, Map<String, List<FunctionDecl>> functions,
      Map<String, Decl> session) {
  }
}
