package org.hl7.fhir.r5.context;

import org.hl7.fhir.r5.model.ValueSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseWorkerContextTsSupportTest {

  private static final String TS_URL = "https://terminologystandardsservice.ca/tx/fhir";

  @Test
  void omitsAnyValueSetFoundExactlyOnceOnTs() {
    ValueSet valueSet = new ValueSet();
    valueSet.setUrl("https://fhir.infoway-inforoute.ca/ValueSet/healthcareproviderspecialtycode");

    assertTrue(BaseWorkerContext.shouldOmitTxResourceForTsValueSet(TS_URL, valueSet, true));

    valueSet.setUrl("https://fhir.infoway-inforoute.ca/ValueSet/another-ts-hosted-valueset");
    assertTrue(BaseWorkerContext.shouldOmitTxResourceForTsValueSet(TS_URL, valueSet, true));
  }

  @Test
  void omitsVersionedValueSetThatExistsOnTs() {
    ValueSet valueSet = new ValueSet();
    valueSet.setUrl("https://fhir.infoway-inforoute.ca/ValueSet/versioned");
    valueSet.setVersion("1.0.0");

    assertTrue(BaseWorkerContext.shouldOmitTxResourceForTsValueSet(TS_URL, valueSet, true));
  }

  @Test
  void keepsValueSetAsTxResourceWhenItIsNotFoundExactlyOnceOnTs() {
    ValueSet valueSet = new ValueSet();
    valueSet.setUrl("https://example.org/ValueSet/local-only");

    assertFalse(BaseWorkerContext.shouldOmitTxResourceForTsValueSet(TS_URL, valueSet, false));
  }

  @Test
  void keepsValueSetAsTxResourceForOtherServers() {
    ValueSet valueSet = new ValueSet();
    valueSet.setUrl("https://fhir.infoway-inforoute.ca/ValueSet/ts-hosted");

    assertFalse(BaseWorkerContext.shouldOmitTxResourceForTsValueSet("https://tx.fhir.org/r4", valueSet, true));
  }

  @Test
  void keepsValueSetAsTxResourceForTsProxy() {
    ValueSet valueSet = new ValueSet();
    valueSet.setUrl("https://fhir.infoway-inforoute.ca/ValueSet/ts-hosted");

    assertFalse(BaseWorkerContext.shouldOmitTxResourceForTsValueSet("https://smart-proxy.apibox.ca:10500/tx/fhir", valueSet, true));
  }
}
