/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server;

import ch.qos.logback.classic.Logger;
import org.junit.Test;
import org.obiba.opal.server.logging.AbstractDatashieldLoggingTest;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * conf/logback.xml belongs to the installation: it is edited there, and an upgrade never overwrites
 * it. An Opal upgraded from a version that predates these appenders therefore exports its traces and
 * its metrics and not one log record - and telling that installation "OpenTelemetry export enabled"
 * and nothing else is how someone ends up looking for logs that were never sent. So the server looks
 * for the appender and says when it is not there.
 */
public class OpalServerLogAppenderTest extends AbstractDatashieldLoggingTest {

  @Test
  public void test_the_shipped_configuration_is_recognised_as_exporting_logs() {
    assertThat(OpalServer.hasOpenTelemetryAppender()).isTrue();
  }

  /**
   * A configuration from before the appenders existed, which is what the upgrade leaves behind.
   */
  @Test
  public void test_a_configuration_without_the_appender_is_noticed() {
    globalContext().getLoggerList().forEach(Logger::detachAndStopAllAppenders);

    assertThat(OpalServer.hasOpenTelemetryAppender()).isFalse();
  }
}
