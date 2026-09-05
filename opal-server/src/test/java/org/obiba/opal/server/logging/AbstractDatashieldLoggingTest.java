/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import org.junit.After;
import org.junit.Before;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.nio.file.Files;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Drives a DataSHIELD audit event through the real conf/logback.xml. The global logger context
 * is used rather than a private one because OpenTelemetryAppender.install() looks the appenders up
 * through LoggerFactory.getILoggerFactory(); it is restored afterwards so the rest of the module's
 * tests are unaffected.
 */
public abstract class AbstractDatashieldLoggingTest {

  protected static final String SCRIPT = "meanDS(D$age)";

  private File logsDir;

  @Before
  public void configureLogbackFromTheShippedFile() throws Exception {
    logsDir = Files.createTempDirectory("opal-logs").toFile();
    System.setProperty("OPAL_LOG", logsDir.getAbsolutePath());

    File config = new File("src/main/conf/logback.xml");
    assertThat(config).exists();

    LoggerContext context = globalContext();
    context.reset();
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(context);
    configurator.doConfigure(config);
  }

  @After
  public void restoreLogback() throws Exception {
    LoggerContext context = globalContext();
    context.stop();
    context.reset();
    new ContextInitializer(context).autoConfig();
    System.clearProperty("OPAL_LOG");
    deleteRecursively(logsDir);
  }

  /**
   * The MDC keys and values DataShieldLog.prepare() sets for an aggregation, plus the ip that
   * DataShieldLog.init() preserves across calls.
   */
  protected void logDatashieldEvent() {
    MDC.put("ip", "10.0.0.7");
    MDC.put("username", "administrator");
    MDC.put("ds_id", "rsession-42");
    MDC.put("ds_profile", "default");
    MDC.put("ds_action", "AGGREGATE");
    MDC.put("ds_symbol", "D");
    MDC.put("ds_eval", SCRIPT);
    try {
      LoggerFactory.getLogger("datashield.user").info("evaluated '{}'", SCRIPT);
    } finally {
      MDC.clear();
    }
    globalContext().getLoggerList().forEach(logger ->
        logger.iteratorForAppenders().forEachRemaining(appender -> {
          // flush the rolling file appender's stream before the file is read back
          if(appender instanceof ch.qos.logback.core.OutputStreamAppender) {
            ((ch.qos.logback.core.OutputStreamAppender<?>) appender).setImmediateFlush(true);
          }
        }));
  }

  /**
   * Every MDC key a real assignment from a table carries: the DataSHIELD ones, plus the two RockSession
   * sets around each call to the R server. This is the widest record the audit stream produces, which is
   * what makes it the one to assert the exported attribute names against.
   */
  protected void logAssignFromTableEvent() {
    MDC.put("ip", "10.0.0.7");
    MDC.put("username", "administrator");
    MDC.put("ds_id", "rsession-42");
    MDC.put("ds_profile", "default");
    MDC.put("ds_action", "ASSIGN");
    MDC.put("ds_symbol", "x");
    MDC.put("ds_table", "CNSIM.CNSIM1");
    MDC.put("r_size", "4");
    MDC.put("r_duration", "388");
    try {
      LoggerFactory.getLogger("datashield.user").info("created symbol '{}' from: '{}'", "x", "x <- opal[CNSIM.CNSIM1]");
    } finally {
      MDC.clear();
    }
  }

  /**
   * The other two assignment sources, from a resource and from a string expression, each carry a
   * key of their own that the table assignment does not.
   */
  protected void logAssignFromResourceEvent() {
    logAssignEvent("ds_resource", "CNSIM.CNSIM1-resource");
  }

  protected void logAssignFromStringEvent() {
    logAssignEvent("ds_expr", "\"hello\"");
  }

  private void logAssignEvent(String sourceKey, String source) {
    MDC.put("ip", "10.0.0.7");
    MDC.put("username", "administrator");
    MDC.put("ds_id", "rsession-42");
    MDC.put("ds_profile", "default");
    MDC.put("ds_action", "ASSIGN");
    MDC.put("ds_symbol", "x");
    MDC.put(sourceKey, source);
    try {
      LoggerFactory.getLogger("datashield.user").info("created symbol '{}' from: '{}'", "x", source);
    } finally {
      MDC.clear();
    }
  }

  /**
   * The MDC keys AbstractRestrictedRScriptROperation sets while parsing a submitted expression. They
   * only ever appear on a PARSE record: DataShieldLog.init() clears them once it has been written.
   */
  protected void logParseEvent() {
    MDC.put("ip", "10.0.0.7");
    MDC.put("username", "administrator");
    MDC.put("ds_id", "rsession-42");
    MDC.put("ds_profile", "default");
    MDC.put("ds_action", "PARSE");
    MDC.put("ds_script_in", "meanDS(D$age)");
    MDC.put("ds_script_out", "base::mean(D$age)");
    MDC.put("ds_map", "meanDS=base::mean");
    try {
      LoggerFactory.getLogger("datashield.user").info("parsed '{}'", "base::mean(D$age)");
    } finally {
      MDC.clear();
    }
  }

  protected LoggerContext globalContext() {
    return (LoggerContext) LoggerFactory.getILoggerFactory();
  }

  private void deleteRecursively(File file) {
    if(file == null || !file.exists()) return;
    File[] children = file.listFiles();
    if(children != null) {
      for(File child : children) deleteRecursively(child);
    }
    //noinspection ResultOfMethodCallIgnored
    file.delete();
  }
}
