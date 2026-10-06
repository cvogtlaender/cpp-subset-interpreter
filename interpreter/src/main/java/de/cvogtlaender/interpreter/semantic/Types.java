package de.cvogtlaender.interpreter.semantic;

import java.util.List;
import java.util.stream.Collectors;

import de.cvogtlaender.interpreter.ast.declaration.ParameterDecl;
import de.cvogtlaender.interpreter.ast.type.ClassType;
import de.cvogtlaender.interpreter.ast.type.NullptrType;
import de.cvogtlaender.interpreter.ast.type.PointerType;
import de.cvogtlaender.interpreter.ast.type.PrimitiveType;
import de.cvogtlaender.interpreter.ast.type.ReferenceType;
import de.cvogtlaender.interpreter.ast.type.Type;

public final class Types {

  public static final PrimitiveType INT = new PrimitiveType(PrimitiveType.Kind.INT);
  public static final PrimitiveType BOOL = new PrimitiveType(PrimitiveType.Kind.BOOL);
  public static final PrimitiveType CHAR = new PrimitiveType(PrimitiveType.Kind.CHAR);
  public static final PrimitiveType STRING = new PrimitiveType(PrimitiveType.Kind.STRING);
  public static final PrimitiveType VOID = new PrimitiveType(PrimitiveType.Kind.VOID);
  public static final NullptrType NULLPTR = new NullptrType();

  private Types() {
  }

  public static Type strip(Type type) {
    return type instanceof ReferenceType r ? r.getReferencedType() : type;
  }

  public static boolean isReference(Type type) {
    return type instanceof ReferenceType;
  }

  public static boolean same(Type a, Type b) {
    return a != null && b != null && a.getName().equals(b.getName());
  }

  public static boolean is(Type type, PrimitiveType.Kind kind) {
    return type instanceof PrimitiveType p && p.getKind() == kind;
  }

  public static boolean isVoid(Type type) {
    return is(type, PrimitiveType.Kind.VOID);
  }

  public static boolean isClass(Type type) {
    return type instanceof ClassType;
  }

  public static boolean isPointer(Type type) {
    return type instanceof PointerType;
  }

  public static boolean isPointerLike(Type type) {
    return type instanceof PointerType || type instanceof NullptrType;
  }

  public static String signature(String name, List<ParameterDecl> parameters) {
    return name + "(" + parameters.stream().map(p -> p.getType().getName()).collect(Collectors.joining(", ")) + ")";
  }

  public static String parameterKey(List<ParameterDecl> parameters) {
    return parameters.stream().map(p -> p.getType().getName()).collect(Collectors.joining(","));
  }
}
