package ch.ahdis.matchbox.util.http;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import ch.ahdis.matchbox.util.CrossVersionResourceUtils;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.utilities.VersionUtilities;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A wrapper around HTTP servlet requests and responses to handle FHIR requests within HAPI.
 *
 * @see ca.uhn.fhir.rest.server.RestfulServerUtils
 */
public class HttpRequestWrapper {

	private static final MatchboxFhirFormat DEFAULT_FORMAT = MatchboxFhirFormat.JSON;
	private static final String PARAM_FHIR_VERSION = "fhirVersion";

	private final HttpServletRequest request;
	private final HttpServletResponse response;

	// Request properties
	private final byte[] requestBody;
	private final @Nullable MatchboxFhirFormat requestFormat;
	private final FhirVersionEnum requestVersion;

	// Response properties
	private final MatchboxFhirFormat responseFormat;
	private final FhirVersionEnum responseVersion;

	public HttpRequestWrapper(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final FhirVersionEnum defaultVersion
	) throws IOException {
		this.request = request;
		this.response = response;

		// Parse the request Content-Type header
		//   - The format may be null if there's no content in the request
		final var parsedContentType = this.parseContentType();
		this.requestFormat = parsedContentType.format;
		this.requestVersion = parsedContentType.versionOr(defaultVersion);

		// Parse the request Accept header
		final var parsedAccept = this.findBestRequestedFormat();
		this.responseFormat = parsedAccept.formatOr(DEFAULT_FORMAT);
		this.responseVersion = parsedAccept.versionOr(defaultVersion);

		// Run some checks
		this.requestBody = request.getInputStream().readAllBytes();
		if (this.requestFormat == null && this.request.getContentLength() > 0) {
			throw new InvalidRequestException("No Content-Type header found in request with body");
		}
	}

	@Nullable
	public Resource parseBodyAsResource() {
		if (this.requestFormat == null || !this.requestFormat.isResourceFormat()) {
			return null;
		}
		return switch (this.requestVersion) {
			case R4 -> CrossVersionResourceUtils.parseR4AsR5(this.requestBody, this.requestFormat);
			case R4B -> CrossVersionResourceUtils.parseR4bAsR5(this.requestBody, this.requestFormat);
			case R5 -> CrossVersionResourceUtils.parseR5(this.requestBody, this.requestFormat);
			default -> throw new MatchboxUnsupportedFhirVersionException("HttpRequestWrapper.parseBodyAsResource",
																							 this.requestVersion);
		};
	}

	// Write an R5 resource in the response, in the request format and FHIR version.
	public void writeResponse(final Resource resource) throws IOException {
		this.response.setContentType("%s;%s=%s".formatted(
			this.responseFormat.getContentType(),
			PARAM_FHIR_VERSION,
			VersionUtilities.getMajMin(this.responseVersion.getFhirVersionString())
		));
		this.response.setCharacterEncoding("UTF-8");

		final var writer = this.response.getWriter();
		switch (this.responseVersion) {
			case R4 -> CrossVersionResourceUtils.serializeR5AsR4(resource, this.responseFormat, writer);
			case R4B -> CrossVersionResourceUtils.serializeR5AsR4b(resource, this.responseFormat, writer);
			case R5 -> CrossVersionResourceUtils.serializeR5(resource, this.responseFormat, writer);
			default -> throw new MatchboxUnsupportedFhirVersionException("HttpRequestWrapper.writeResponse",
																							 this.responseVersion);
		}
	}

	// Initialize and return a BundleProvider, convert a list of R5 resources to the requested FHIR version.
	public SimpleBundleProvider makeBundleProviderFromR5(final List<Resource> resources) {
		 final var convertedResources = switch (this.responseVersion) {
			case R4 -> resources
						.parallelStream()
						.map(VersionConvertorFactory_40_50::convertResource)
						.toList();
			case R4B -> resources
					.parallelStream()
					.map(VersionConvertorFactory_43_50::convertResource)
					.toList();
			case R5 -> resources;
			default -> throw new MatchboxUnsupportedFhirVersionException("HttpRequestWrapper.makeBundleProviderFromR5",
																							 this.responseVersion);
		};
		return new SimpleBundleProvider(convertedResources);
	}

	// Initialize and return a BundleProvider, convert a list of base resources to the requested FHIR version.
	// Preferably use makeBundleProviderFromR5 instead.
	public SimpleBundleProvider makeBundleProvider(final List<IBaseResource> resources) {
		final List<? extends IBaseResource> convertedResources = switch (this.responseVersion) {
			case R4 -> resources
				.parallelStream()
				.map(resource -> switch (resource) {
					case org.hl7.fhir.r4.model.Resource r4Resource -> r4Resource;
					case org.hl7.fhir.r4b.model.Resource r4bResource -> CrossVersionResourceUtils.convertResource(r4bResource);
					case Resource r5Resource -> VersionConvertorFactory_40_50.convertResource(r5Resource);
					default -> throw new MatchboxUnsupportedFhirVersionException("HttpRequestWrapper.makeBundleProvider.R4",
																									 this.responseVersion);
				})
				.toList();
			case R4B -> resources
				.parallelStream()
				.map(resource -> switch (resource) {
					case org.hl7.fhir.r4.model.Resource r4Resource -> CrossVersionResourceUtils.convertResource(r4Resource);
					case org.hl7.fhir.r4b.model.Resource r4bResource -> r4bResource;
					case Resource r5Resource -> VersionConvertorFactory_43_50.convertResource(r5Resource);
					default -> throw new MatchboxUnsupportedFhirVersionException("HttpRequestWrapper.makeBundleProvider.R4B",
																									 this.responseVersion);
				})
				.toList();
			case R5 -> resources
				.parallelStream()
				.map(resource -> switch (resource) {
					case org.hl7.fhir.r4.model.Resource r4Resource -> VersionConvertorFactory_40_50.convertResource(r4Resource);
					case org.hl7.fhir.r4b.model.Resource r4bResource -> VersionConvertorFactory_43_50.convertResource(r4bResource);
					case Resource r5Resource -> r5Resource;
					default -> throw new MatchboxUnsupportedFhirVersionException("HttpRequestWrapper.makeBundleProvider.R5",
																									 this.responseVersion);
				})
				.toList();
			default -> throw new MatchboxUnsupportedFhirVersionException("HttpRequestWrapper.makeBundleProvider",
																							 this.responseVersion);
		};
		return new SimpleBundleProvider(convertedResources);
	}

	/**
	 * Parse the 'Content-Type' header to find the content FHIR media type.
	 * Throw if the media type is not supported.
	 */
	private FhirMediaType parseContentType() {
		final var contentType = this.request.getContentType();
		if (contentType == null || contentType.isEmpty()) {
			return new FhirMediaType(null, null);
		}
		final var mediaType = MediaType.parseMediaType(contentType);
		final var format = this.mapMediaType(mediaType);
		if (format == null) {
			throw new InvalidRequestException(
				"Unsupported Content-Type: '%s'. Supported types are: %s".formatted(
					contentType,
					Arrays.stream(MatchboxFhirFormat.values()).map(MatchboxFhirFormat::getContentType).collect(Collectors.joining(", "))
				)
			);
		}
		return format;
	}

	/**
	 * Parse the '_format' parameter or the 'Accept' header to find the best matching media type.
	 */
	private FhirMediaType findBestRequestedFormat() {
		// Check the '_format' parameter first
		final var formatParam = this.request.getParameter("_format");
		if (formatParam != null) {
			final var format = this.mapMediaType(MediaType.parseMediaType(formatParam));
			if (format == null) {
				throw new InvalidRequestException(
					"Unsupported _format parameter: '%s'. Supported types are: %s".formatted(
						formatParam,
						Arrays.stream(MatchboxFhirFormat.values()).map(MatchboxFhirFormat::getContentType).collect(Collectors.joining(", "))
					)
				);
			}
			return format;
		}

		// Check the 'Accept' header second
		final var acceptHeader = this.request.getHeader("Accept");
		if (acceptHeader != null) {
			// Parse and sort media types by quality value
			final List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader);
			mediaTypes.sort(Comparator.comparingDouble(MediaType::getQualityValue).reversed());

			// Find the first media type that matches our supported formats
			for (final var mediaType : mediaTypes) {
				final var format = this.mapMediaType(mediaType);
				if (format != null) {
					return format;
				}
			}
		}

		// Use default values
		return new FhirMediaType(null, null);
	}

	@Nullable
	private FhirMediaType mapMediaType(final MediaType mediaType) {
		final var format = switch (mediaType.getType()) {
			case "application" -> switch (mediaType.getSubtype()) {
				case "fhir+json", "json+fhir", "json" -> MatchboxFhirFormat.JSON;
				case "fhir+xml", "xml+fhir", "xml" -> MatchboxFhirFormat.XML;
				default -> null;
			};
			case "json" -> MatchboxFhirFormat.JSON;
			case "xml" -> MatchboxFhirFormat.XML;
			default -> null;
		};
		if (format == null) {
			return null;
		}
		final var fhirVersion = mediaType.getParameter(PARAM_FHIR_VERSION);
		return new FhirMediaType(
			format,
			(fhirVersion != null) ? FhirVersionEnum.forVersionString(fhirVersion) : null
		);
	}

	/**
	 * A record to hold the parsed FHIR media type.
	 */
	private record FhirMediaType(@Nullable MatchboxFhirFormat format,
										  @Nullable FhirVersionEnum version) {

		public MatchboxFhirFormat formatOr(final MatchboxFhirFormat defaultFormat) {
			return (this.format != null) ? this.format : defaultFormat;
		}

		public FhirVersionEnum versionOr(final FhirVersionEnum defaultVersion) {
			return (this.version != null) ? this.version : defaultVersion;
		}
	}
}
