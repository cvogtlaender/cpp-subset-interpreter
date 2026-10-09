package de.cvogtlaender.lsp;

import java.util.List;
import java.util.stream.Collectors;

import de.cvogtlaender.interpreter.ast.declaration.ClassDecl;
import de.cvogtlaender.interpreter.ast.declaration.ConstructorDecl;
import de.cvogtlaender.interpreter.ast.declaration.Decl;
import de.cvogtlaender.interpreter.ast.declaration.FieldDecl;
import de.cvogtlaender.interpreter.ast.declaration.FunctionDecl;
import de.cvogtlaender.interpreter.ast.declaration.MethodDecl;
import de.cvogtlaender.interpreter.ast.declaration.ParameterDecl;
import de.cvogtlaender.interpreter.ast.declaration.VariableDecl;
import de.cvogtlaender.interpreter.ast.type.Type;
import de.cvogtlaender.interpreter.semantic.Types;

public final class Names {

  private Names() {
  }

  public static String of(Decl decl) {
    return switch (decl) {
      case ClassDecl c -> c.getClassName();
      case ConstructorDecl k -> k.getName();
      case FieldDecl f -> f.getName();
      case FunctionDecl f -> f.getName();
      case MethodDecl m -> m.getName();
      case ParameterDecl p -> p.getName();
      case VariableDecl v -> v.getName();
      default -> "";
    };
  }

  public static Type typeOf(Decl decl) {
    return switch (decl) {
      case FieldDecl f -> f.getType();
      case ParameterDecl p -> p.getType();
      case VariableDecl v -> v.getType();
      case FunctionDecl f -> f.getReturnType();
      case MethodDecl m -> m.getReturnType();
      default -> null;
    };
  }

  public static String signature(Decl decl) {
    return switch (decl) {
      case ClassDecl c -> "class " + c.getClassName()
          + (c.getParentClassName() == null ? "" : " : public " + c.getParentClassName());
      case ConstructorDecl k -> qualified(k.getOwner(), k.getName()) + parameters(k.getParameters());
      case FieldDecl f -> declarator(f.getType(), qualified(f.getOwner(), f.getName()));
      case FunctionDecl f -> declarator(f.getReturnType(), f.getName()) + parameters(f.getParameters());
      case MethodDecl m -> (m.isEffectivelyVirtual() || m.getIsVirtual() ? "virtual " : "")
          + declarator(m.getReturnType(), qualified(m.getOwner(), m.getName())) + parameters(m.getParameters());
      case ParameterDecl p -> declarator(p.getType(), p.getName());
      case VariableDecl v -> declarator(v.getType(), v.getName());
      default -> "";
    };
  }

  public static String detail(Decl decl) {
    return switch (decl) {
      case FunctionDecl f -> f.getReturnType().getName() + " " + f.getName() + parameters(f.getParameters());
      case MethodDecl m -> m.getReturnType().getName() + " " + m.getName() + parameters(m.getParameters());
      case ConstructorDecl k -> k.getName() + parameters(k.getParameters());
      case ClassDecl c -> signature(c);
      default -> {
        Type type = typeOf(decl);
        yield type == null ? "" : type.getName();
      }
    };
  }

  public static String kind(Decl decl) {
    return switch (decl) {
      case ClassDecl c -> "class";
      case ConstructorDecl k -> k.isSynthesized() ? "implicit default constructor" : "constructor";
      case FieldDecl f -> "field";
      case FunctionDecl f -> f.isBuiltin() ? "built-in function" : "function";
      case MethodDecl m -> m.isEffectivelyVirtual() ? "virtual method" : "method";
      case ParameterDecl p -> "parameter";
      case VariableDecl v -> "local variable";
      default -> "";
    };
  }

  public static MethodDecl overridden(MethodDecl m) {
    if (m.getOwner() == null) {
      return null;
    }
    String key = Types.parameterKey(m.getParameters());
    for (ClassDecl c = m.getOwner().getParent(); c != null; c = c.getParent()) {
      for (MethodDecl candidate : c.getMethods()) {
        if (candidate.getName().equals(m.getName()) && Types.parameterKey(candidate.getParameters()).equals(key)) {
          return candidate.isEffectivelyVirtual() ? candidate : null;
        }
      }
    }
    return null;
  }

  private static String qualified(ClassDecl owner, String name) {
    return owner == null ? name : owner.getClassName() + "::" + name;
  }

  private static String declarator(Type type, String name) {
    return type.getName() + " " + name;
  }

  private static String parameters(List<ParameterDecl> parameters) {
    return parameters.stream().map(p -> declarator(p.getType(), p.getName()))
        .collect(Collectors.joining(", ", "(", ")"));
  }
}
