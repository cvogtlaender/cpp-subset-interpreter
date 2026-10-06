package de.cvogtlaender.mcp.provider;

public interface GenAiProvider {

  String name();

  String model();

  String generate(String system, String prompt) throws GenAiException;

  boolean isAvailable();
}
