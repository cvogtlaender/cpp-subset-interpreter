package de.cvogtlaender.mcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import de.cvogtlaender.mcp.provider.GenAiException;

public class McpServer {

  private static final int MAX_BODY_BYTES = 1 << 20;

  private final AssistantService assistant;
  private final HttpServer server;
  private final ExecutorService executor;
  private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

  private interface Handler {
    Object handle(JsonObject body) throws GenAiException;
  }

  /** Thrown for invalid requests; mapped to HTTP 400. */
  static final class BadRequest extends RuntimeException {
    BadRequest(String message) {
      super(message);
    }
  }

  public McpServer(AssistantService assistant, String host, int port) throws IOException {
    this.assistant = assistant;
    this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
    this.executor = Executors.newFixedThreadPool(4);
    server.setExecutor(executor);

    post("/complete", body -> assistant.complete(requireString(body, "code"), requireInt(body, "line"),
        requireInt(body, "column")));
    post("/explain", body -> assistant.explain(requireString(body, "code")));
    post("/refactor", body -> assistant.refactor(requireString(body, "code"), optionalString(body, "instruction")));
    post("/detect-bugs", body -> assistant.detectBugs(requireString(body, "code")));
    server.createContext("/health", this::health);
    server.createContext("/", exchange -> send(exchange, 404, error("unknown endpoint " + exchange.getRequestURI())));
  }

  public void start() {
    server.start();
  }

  public void stop() {
    server.stop(0);
    executor.shutdownNow();
  }

  public int port() {
    return server.getAddress().getPort();
  }

  private void post(String path, Handler handler) {
    server.createContext(path, exchange -> {
      try (exchange) {
        if (!exchange.getRequestURI().getPath().equals(path)) {
          send(exchange, 404, error("unknown endpoint " + exchange.getRequestURI()));
          return;
        }
        if (!exchange.getRequestMethod().equals("POST")) {
          exchange.getResponseHeaders().add("Allow", "POST");
          send(exchange, 405, error("use POST"));
          return;
        }
        JsonObject body;
        try {
          body = readJson(exchange);
        } catch (BadRequest e) {
          send(exchange, e.getMessage().startsWith("request body exceeds") ? 413 : 400, error(e.getMessage()));
          return;
        }
        try {
          send(exchange, 200, gson.toJsonTree(handler.handle(body)));
        } catch (BadRequest | IllegalArgumentException e) {
          send(exchange, 400, error(e.getMessage()));
        } catch (GenAiException e) {
          send(exchange, 502, error(e.getMessage()));
        } catch (RuntimeException e) {
          send(exchange, 500, error("internal error: " + e));
        }
      }
    });
  }

  private void health(HttpExchange exchange) throws IOException {
    try (exchange) {
      if (!exchange.getRequestMethod().equals("GET")) {
        exchange.getResponseHeaders().add("Allow", "GET");
        send(exchange, 405, error("use GET"));
        return;
      }
      boolean available = assistant.provider().isAvailable();
      Map<String, Object> status = new LinkedHashMap<>();
      status.put("status", available ? "ok" : "degraded");
      status.put("provider", assistant.provider().name());
      status.put("model", assistant.provider().model());
      status.put("providerAvailable", available);
      send(exchange, 200, gson.toJsonTree(status));
    }
  }

  private static JsonObject readJson(HttpExchange exchange) throws IOException {
    byte[] bytes;
    try (InputStream in = exchange.getRequestBody()) {
      bytes = in.readNBytes(MAX_BODY_BYTES + 1);
    }
    if (bytes.length > MAX_BODY_BYTES) {
      throw new BadRequest("request body exceeds " + MAX_BODY_BYTES + " bytes");
    }
    try {
      JsonElement json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
      if (!json.isJsonObject()) {
        throw new BadRequest("request body must be a JSON object");
      }
      return json.getAsJsonObject();
    } catch (JsonParseException e) {
      throw new BadRequest("invalid JSON: " + e.getMessage());
    }
  }

  static String requireString(JsonObject body, String field) {
    JsonElement e = body.get(field);
    if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
      throw new BadRequest("missing string field '" + field + "'");
    }
    return e.getAsString();
  }

  static String optionalString(JsonObject body, String field) {
    return body.has(field) && !body.get(field).isJsonNull() ? requireString(body, field) : null;
  }

  static int requireInt(JsonObject body, String field) {
    JsonElement e = body.get(field);
    if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) {
      throw new BadRequest("missing integer field '" + field + "'");
    }
    return e.getAsInt();
  }

  private static JsonObject error(String message) {
    JsonObject json = new JsonObject();
    json.addProperty("error", message);
    return json;
  }

  private void send(HttpExchange exchange, int status, JsonElement json) throws IOException {
    byte[] bytes = gson.toJson(json).getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(bytes);
    }
  }
}
