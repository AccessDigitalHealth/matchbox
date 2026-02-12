package ch.ahdis.matchbox;

import org.hl7.fhir.r5.context.ILoggingService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EngineLoggingService implements ILoggingService {

  protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EngineLoggingService.class);

  private final boolean debug;

  public EngineLoggingService() {
	  debug = true;
//    this(false);
  }

  @Override
  public void logMessage(String message) {
    log.info(message);
  }

  @Override
  public void logDebugMessage(LogCategory category, String message) {
	 if (LogCategory.TX.equals(category)) {
		 log.debug(" -" + category.name().toLowerCase() + ": " + message);
	 }
    else if (debug) {
		 log.debug(" -" + category.name().toLowerCase() + ": " + message);
    }
  }

}
