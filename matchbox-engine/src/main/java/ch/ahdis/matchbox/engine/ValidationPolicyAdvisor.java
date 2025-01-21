package ch.ahdis.matchbox.engine;


import org.hl7.fhir.r5.utils.validation.constants.ReferenceValidationPolicy;
import org.hl7.fhir.validation.instance.advisor.BasePolicyAdvisorForFullValidation;

public class ValidationPolicyAdvisor extends BasePolicyAdvisorForFullValidation {

    public ValidationPolicyAdvisor(ReferenceValidationPolicy refpol) {
        super(refpol);
    }

    @Override
    public boolean isSuppressMessageId(String path, String messageId) {
      return false;
    }

}
