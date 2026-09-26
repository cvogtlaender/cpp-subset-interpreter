package de.cvogtlaender.interpreter.runtime;

import de.cvogtlaender.interpreter.ast.AstNode;
import de.cvogtlaender.interpreter.diagnostic.Diagnostic;

/** An error during program execution, e.g. a division by zero. */
public class MiniCppRuntimeException extends RuntimeException {

  private final transient AstNode node;

  public MiniCppRuntimeException(String message, AstNode node) {
    super(message);
    this.node = node;
  }

  public AstNode getNode() {
    return node;
  }

  public Diagnostic toDiagnostic() {
    return Diagnostic.at(Diagnostic.Phase.RUNTIME, node, getMessage());
  }
}
