package ch.ahdis.matchbox.validation;

import ch.ahdis.matchbox.CliContext;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationProviderTsFilteringTest {

  private static final String SUPPLEMENT_ERROR =
    "Required supplement not found: https://fhir.infoway-inforoute.ca/CodeSystem/Supplement/fr-CA/task-status|1.0.0";
  private static final String REFERRAL_BUSINESS_STATUS_ERROR =
    "None of the codings provided are in the value set 'ReferralBusinessStatus' " +
      "(https://fhir.infoway-inforoute.ca/ValueSet/ca-referralbusinessstatus|1.2.0), " +
      "and a coding should come from this value set unless it has no suitable code " +
      "(codes = http://hl7.org/fhir/task-status#requested)";

  @Test
  void recognizesOnlyKnownSupplementFalsePositiveMessages() {
    ValidationProvider provider = new ValidationProvider();

    assertTrue(provider.isTsSupplementFalsePositive(SUPPLEMENT_ERROR));
    assertTrue(provider.isTsSupplementFalsePositive(REFERRAL_BUSINESS_STATUS_ERROR));
    assertFalse(provider.isTsSupplementFalsePositive("Required supplement not found: https://example.org/other"));
    assertFalse(provider.isTsSupplementFalsePositive("None of the codings provided are in the value set 'Other'"));
  }

  @Test
  void filtersKnownFalsePositivesOnlyForTs() {
    ValidationProvider provider = providerWithTxServer("https://terminologystandardsservice.ca/tx/fhir");

    List<ValidationMessage> messages = new ArrayList<>();
    messages.add(message(SUPPLEMENT_ERROR));
    messages.add(message(REFERRAL_BUSINESS_STATUS_ERROR));
    messages.add(message("A real validation problem"));

    provider.filterTsSupplementFalsePositives(messages);

    assertEquals(1, messages.size());
    assertEquals("A real validation problem", messages.get(0).getMessage());
  }

  @Test
  void doesNotFilterKnownMessagesForNonTsServers() {
    ValidationProvider provider = providerWithTxServer("https://tx.fhir.org/r4");

    List<ValidationMessage> messages = new ArrayList<>();
    messages.add(message(SUPPLEMENT_ERROR));

    provider.filterTsSupplementFalsePositives(messages);

    assertEquals(1, messages.size());
  }

  @Test
  void doesNotRecognizeTsProxyServer() {
    ValidationProvider provider = providerWithTxServer("https://smart-proxy.apibox.ca:10500/tx/fhir");

    assertFalse(provider.isInfowayTsServer());
  }

  @Test
  void recognizesTerminologyDiagnosticsForLogging() {
    ValidationProvider provider = new ValidationProvider();

    assertTrue(provider.isTerminologyDiagnostic("ValueSet expansion failed because tx-resource could not be processed"));
    assertTrue(provider.isTerminologyDiagnostic("Required supplement not found"));
    assertTrue(provider.isTerminologyDiagnostic("Terminology server returned HTTP 401 unauthorized"));
    assertFalse(provider.isTerminologyDiagnostic("Patient.name is missing"));
  }

  private ValidationProvider providerWithTxServer(String txServer) {
    ValidationProvider provider = new ValidationProvider();
    provider.cliContext = new CliContext(new MockEnvironment());
    provider.cliContext.setTxServer(txServer);
    return provider;
  }

  private ValidationMessage message(String text) {
    ValidationMessage message = new ValidationMessage();
    message.setMessage(text);
    return message;
  }
}
