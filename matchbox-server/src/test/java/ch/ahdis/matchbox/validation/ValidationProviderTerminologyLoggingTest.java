package ch.ahdis.matchbox.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationProviderTerminologyLoggingTest {

  @Test
  void recognizesTerminologyDiagnosticsForLogging() {
    ValidationProvider provider = new ValidationProvider();

    assertTrue(provider.isTerminologyDiagnostic("ValueSet expansion failed because tx-resource could not be processed"));
    assertTrue(provider.isTerminologyDiagnostic("Required supplement not found"));
    assertTrue(provider.isTerminologyDiagnostic("Terminology server returned HTTP 401 unauthorized"));
    assertFalse(provider.isTerminologyDiagnostic("Patient.name is missing"));
  }
}
