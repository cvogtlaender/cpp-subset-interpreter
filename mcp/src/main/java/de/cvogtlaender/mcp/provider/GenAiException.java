package de.cvogtlaender.mcp.provider;

public class GenAiException extends Exception {

  public GenAiException(String message) {
    super(message);
  }

  public GenAiException(String message, Throwable cause) {
    super(message, cause);
  }
}
