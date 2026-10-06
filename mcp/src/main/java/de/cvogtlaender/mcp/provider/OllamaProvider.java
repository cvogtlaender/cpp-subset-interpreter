package de.cvogtlaender.mcp.provider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OllamaProvider implements GenAiProvider {

  private final HttpClient http;
  private final URI baseUrl;
  private final String model;
  private final Duration timeout;

  public OllamaProvider(String baseUrl, String model, Duration timeout) {
    this.baseUrl = URI.create(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/");
    this.model = model;
    this.timeout = timeout;
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  }

  @Override
  public String name() {
    return "ollama";
  }

  @Override
  public String model() {
    return model;
  }

  @Override
  public String generate(String system, String prompt) throws GenAiException {
    JsonObject body = new JsonObject();
    body.addProperty("model", model);
    body.addProperty("system", system);
    body.addProperty("prompt", prompt);
    body.addProperty("stream", false);
    JsonObject options = new JsonObject();
    options.addProperty("temperature", 0.2);
    body.add("options", options);

    HttpRequest request = HttpRequest.newBuilder(baseUrl.resolve("api/generate"))
        .timeout(timeout)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
        .build();

    HttpResponse<String> response;
    try {
      response = http.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
      throw new GenAiException("Ollama is not reachable at " + baseUrl + ": " + e.getMessage(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new GenAiException("interrupted while waiting for Ollama", e);
    }

    if (response.statusCode() != 200) {
      throw new GenAiException("Ollama returned HTTP " + response.statusCode() + ": " + response.body());
    }
    try {
      JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
      if (json.has("error")) {
        throw new GenAiException("Ollama error: " + json.get("error").getAsString());
      }
      return json.get("response").getAsString();
    } catch (RuntimeException e) {
      throw new GenAiException("unexpected response from Ollama: " + response.body(), e);
    }
  }

  @Override
  public boolean isAvailable() {
    HttpRequest request = HttpRequest.newBuilder(baseUrl.resolve("api/tags"))
        .timeout(Duration.ofSeconds(3))
        .GET()
        .build();
    try {
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        return false;
      }
      JsonArray models = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("models");
      for (JsonElement m : models) {
        String name = m.getAsJsonObject().get("name").getAsString();
        if (name.equals(model) || name.startsWith(model + ":")) {
          return true;
        }
      }
      return false;
    } catch (IOException | RuntimeException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
