package org.hl7.fhir.r5.context;

import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.PackageInformation;
import org.hl7.fhir.r5.model.ValueSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

  @Test
  void tagsLiveTsValueSetWithPackageMetadataBeforeCaching() {
    ValueSet valueSet = new ValueSet();

    PackageInformation packageInfo = BaseWorkerContext.prepareTerminologyServerResource(valueSet, "4.0.1", TS_URL);

    assertNotNull(packageInfo);
    assertTrue(valueSet.hasSourcePackage());
    assertSame(packageInfo, valueSet.getSourcePackage());
    assertEquals("matchbox.terminology-server", packageInfo.getId());
    assertEquals("live", packageInfo.getVersion());
    assertEquals("matchbox.terminology-server#live", packageInfo.getVID());
    assertEquals("4.0.1", packageInfo.getFhirVersion());
    assertEquals(TS_URL, packageInfo.getCanonical());
  }

  @Test
  void tagsLiveTsSupplementCodeSystemWithPackageMetadataBeforeCaching() {
    CodeSystem supplement = new CodeSystem();

    PackageInformation packageInfo = BaseWorkerContext.prepareTerminologyServerResource(supplement, "4.0.1", TS_URL);

    assertTrue(supplement.hasSourcePackage());
    assertSame(packageInfo, supplement.getSourcePackage());
    assertEquals("matchbox.terminology-server#live", supplement.getSourcePackage().getVID());
  }

  @Test
  void keepsExistingPackageMetadataOnLiveTsResource() {
    ValueSet valueSet = new ValueSet();
    PackageInformation existing = new PackageInformation("example.package", "1.2.3", "4.0.1", new java.util.Date(0));
    valueSet.setSourcePackage(existing);

    PackageInformation packageInfo = BaseWorkerContext.prepareTerminologyServerResource(valueSet, "4.0.1", TS_URL);

    assertSame(existing, packageInfo);
    assertSame(existing, valueSet.getSourcePackage());
  }
}
