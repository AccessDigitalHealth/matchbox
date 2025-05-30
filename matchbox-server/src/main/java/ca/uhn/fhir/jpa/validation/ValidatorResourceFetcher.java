/*-
 * #%L
 * HAPI FHIR Storage api
 * %%
 * Copyright (C) 2014 - 2025 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ca.uhn.fhir.jpa.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.common.hapi.validation.validator.VersionSpecificWorkerContextWrapper;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.JsonParser;
import org.hl7.fhir.r5.model.CanonicalResource;
import org.hl7.fhir.r5.model.CanonicalType;
import org.hl7.fhir.r5.utils.validation.IResourceValidator;
import org.hl7.fhir.r5.utils.validation.IValidatorResourceFetcher;
import org.hl7.fhir.utilities.CanonicalPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Please note that this bean is not currently used as part of the $validate operation.
 * The FHIR Core validation library uses {@link VersionSpecificWorkerContextWrapper} to retrieve validation resources.
 */
public class ValidatorResourceFetcher implements IValidatorResourceFetcher {

	private static final Logger ourLog = LoggerFactory.getLogger(ValidatorResourceFetcher.class);

	public ValidatorResourceFetcher() {
	}

	@Override
	public Element fetch(IResourceValidator iResourceValidator, Object appContext, String theUrl) throws FHIRException {
		// matchbox 3.1.0: we don't use validator resource fetcher
		return null;
	}

	@Override
	public
	boolean resolveURL(IResourceValidator validator, Object appContext, String path, String url, String type, boolean canonical, List<CanonicalType> targets) throws IOException, FHIRException {
		return true;
	}

	@Override
	public byte[] fetchRaw(IResourceValidator iResourceValidator, String s) throws UnsupportedOperationException {
		throw new UnsupportedOperationException(Msg.code(577));
	}

	@Override
	public IValidatorResourceFetcher setLocale(Locale locale) {
		// ignore
		return this;
	}

	@Override
	public CanonicalResource fetchCanonicalResource(IResourceValidator validator, Object appContext, String url) {
		return null;
	}

	@Override
	public boolean fetchesCanonicalResource(IResourceValidator iResourceValidator, String s) {
		return false;
	}

	@Override
	public Set<String> fetchCanonicalResourceVersions(IResourceValidator validator, Object appContext, String url) {
		return Collections.emptySet();
	}
}