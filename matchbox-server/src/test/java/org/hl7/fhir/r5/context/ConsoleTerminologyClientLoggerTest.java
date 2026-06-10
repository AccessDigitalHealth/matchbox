package org.hl7.fhir.r5.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

class ConsoleTerminologyClientLoggerTest {

  @Test
  void redactsKnownSecretHeaders() {
    List<String> headers = ConsoleTerminologyClientLogger.redactHeaders(List.of(
      "Authorization: Bearer secret",
      "apiKey: abc123",
      "hubtoken=secret-hub",
      "Content-Type: application/fhir+json"
    ));

    assertEquals("Authorization: [REDACTED]", headers.get(0));
    assertEquals("apiKey: [REDACTED]", headers.get(1));
    assertEquals("hubtoken=[REDACTED]", headers.get(2));
    assertEquals("Content-Type: application/fhir+json", headers.get(3));
  }

  @Test
  void redactsKnownSecretValuesInUrlsAndBodies() {
    String redacted = ConsoleTerminologyClientLogger.redact(
      "https://example.org/tx?access_token=secret-token&ok=true {\"token\":\"body-secret\"}");

    assertTrue(redacted.contains("access_token=[REDACTED]"));
    assertTrue(redacted.contains("\"token\":\"[REDACTED]\""));
    assertFalse(redacted.contains("secret-token"));
    assertFalse(redacted.contains("body-secret"));
  }

  @Test
  void truncatesRedactedBodies() {
    ConsoleTerminologyClientLogger logger = new ConsoleTerminologyClientLogger(10);

    String body = logger.present("0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    assertEquals("0123456789...[truncated 6 chars]", body);
  }
}
