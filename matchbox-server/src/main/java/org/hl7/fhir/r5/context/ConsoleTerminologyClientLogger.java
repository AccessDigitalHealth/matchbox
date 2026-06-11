package org.hl7.fhir.r5.context;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.hl7.fhir.utilities.ToolingClientLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleTerminologyClientLogger extends BaseLogger implements ToolingClientLogger {

  public static final int DEFAULT_BODY_LIMIT = 4000;

  private static final Logger log = LoggerFactory.getLogger(ConsoleTerminologyClientLogger.class);
  private static final Pattern SECRET_JSON =
      Pattern.compile("(?i)(\"(?:authorization|apikey|api_key|hubtoken|token|access_token)\"\\s*:\\s*\")([^\"]*)(\")");
  private static final Pattern SECRET_ASSIGNMENT =
      Pattern.compile("(?i)\\b(authorization|apikey|api_key|hubtoken|token|access_token)(\\s*[=:]\\s*)([^\\s&;,\"'}]+)");

  private final int bodyLimit;

  public ConsoleTerminologyClientLogger() {
    this(DEFAULT_BODY_LIMIT);
  }

  public ConsoleTerminologyClientLogger(int bodyLimit) {
    this.bodyLimit = Math.max(0, bodyLimit);
  }

  @Override
  public void logRequest(String method, String url, List<String> headers, byte[] body) {
    String id = nextId();
    log.info("TX request {} {} {}", id, method, redact(url));
    log.info("TX request {} headers: {}", id, redactHeaders(headers));
    log.info("TX request {} body: {}", id, present(body));
  }

  @Override
  public void logResponse(String outcome, List<String> headers, byte[] body, long start) {
    String id = getLastId() == null ? "unknown" : getLastId();
    long elapsed = elapsedMillis(start);
    if (elapsed >= 0) {
      log.info("TX response {} outcome={} elapsed={}ms", id, outcome, elapsed);
    } else {
      log.info("TX response {} outcome={}", id, outcome);
    }
    log.info("TX response {} headers: {}", id, redactHeaders(headers));
    log.info("TX response {} body: {}", id, present(body));
  }

  String present(byte[] body) {
    if (body == null) {
      return "";
    }
    return truncate(redact(new String(body, StandardCharsets.UTF_8)), bodyLimit);
  }

  static String redact(String text) {
    if (text == null) {
      return null;
    }
    String redacted = SECRET_JSON.matcher(text).replaceAll("$1[REDACTED]$3");
    return SECRET_ASSIGNMENT.matcher(redacted).replaceAll("$1$2[REDACTED]");
  }

  static List<String> redactHeaders(List<String> headers) {
    if (headers == null) {
      return List.of();
    }
    List<String> redacted = new ArrayList<>();
    for (String header : headers) {
      redacted.add(redactHeader(header));
    }
    return redacted;
  }

  static String redactHeader(String header) {
    if (header == null) {
      return null;
    }
    int separator = header.indexOf(':');
    if (separator < 0) {
      separator = header.indexOf('=');
    }
    if (separator > -1 && isSecretName(header.substring(0, separator))) {
      String replacement = header.charAt(separator) == ':' ? " [REDACTED]" : "[REDACTED]";
      return header.substring(0, separator + 1) + replacement;
    }
    return redact(header);
  }

  static String truncate(String text, int limit) {
    if (text == null || text.length() <= limit) {
      return text;
    }
    return text.substring(0, limit) + "...[truncated " + (text.length() - limit) + " chars]";
  }

  private static boolean isSecretName(String name) {
    if (name == null) {
      return false;
    }
    String normalized = name.trim().replace("-", "").replace("_", "").toLowerCase();
    return normalized.equals("authorization")
        || normalized.equals("apikey")
        || normalized.equals("hubtoken")
        || normalized.equals("token")
        || normalized.equals("accesstoken");
  }

  private static long elapsedMillis(long start) {
    long now = System.currentTimeMillis();
    if (start <= 0 || start > now) {
      return -1;
    }
    return now - start;
  }
}
