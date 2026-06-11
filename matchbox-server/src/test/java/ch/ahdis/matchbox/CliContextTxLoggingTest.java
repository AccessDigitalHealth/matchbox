package ch.ahdis.matchbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class CliContextTxLoggingTest {

  @Test
  void usesTxLoggingDefaults() {
    CliContext cliContext = new CliContext(new MockEnvironment());

    assertTrue(cliContext.isTxLogToConsole());
    assertEquals(4000, cliContext.getTxLogConsoleBodyLimitValue());
  }

  @Test
  void readsTxLoggingEnvironmentAliases() {
    MockEnvironment environment = new MockEnvironment()
      .withProperty("TX_LOG_TO_CONSOLE", "false")
      .withProperty("TX_LOG_CONSOLE_BODY_LIMIT", "123")
      .withProperty("TX_LOG_PATH", "tx.log");

    CliContext cliContext = new CliContext(environment);

    assertFalse(cliContext.isTxLogToConsole());
    assertEquals(123, cliContext.getTxLogConsoleBodyLimitValue());
    assertEquals("tx.log", cliContext.getEffectiveTxLogPath());
  }

  @Test
  void fallsBackToDefaultBodyLimitWhenConfiguredValueIsInvalid() {
    CliContext cliContext = new CliContext(new MockEnvironment()
      .withProperty("TX_LOG_CONSOLE_BODY_LIMIT", "not-a-number"));

    assertEquals(4000, cliContext.getTxLogConsoleBodyLimitValue());
  }
}
