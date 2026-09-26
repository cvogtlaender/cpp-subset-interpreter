package de.cvogtlaender.mcp.provider;

/** The GenAI backend failed or is unreachable. */
public class GenAiException extends Exception {

  public GenAiException(String message) {
    super(message);
  }

  public GenAiException(String message, Throwable cause) {
    super(message, cause);
  }
}
