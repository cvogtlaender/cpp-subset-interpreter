package de.cvogtlaender.mcp.provider;

/** A text generation backend (Ollama in production, a mock in tests). */
public interface GenAiProvider {

  /** Short provider name, e.g. "ollama". */
  String name();

  /** Model used for generation. */
  String model();

  /** Generates a completion for {@code prompt} under the given system instructions. */
  String generate(String system, String prompt) throws GenAiException;

  /** Whether the backend is reachable and the model is available. */
  boolean isAvailable();
}
