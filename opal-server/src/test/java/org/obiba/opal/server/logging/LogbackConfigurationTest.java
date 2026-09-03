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
import ch.qos.logback.core.status.Status;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Logback reports an unknown element by writing a warning to its status manager and carrying on, so a
 * configuration file can name properties that do not exist and still look like it works. The example
 * OpenTelemetry configuration shipped in 5.7 did exactly that - it set endpoint, resourceAttributes
 * and flushInterval on an appender that has none of them - and nothing failed until someone went
 * looking for the logs that were never exported. This file is shipped to users, so parse it and
 * treat any non-INFO status as a build failure.
 */
public class LogbackConfigurationTest {

  @Rule
  public TemporaryFolder logs = new TemporaryFolder();

  @Test
  public void test_the_shipped_configuration_has_no_configuration_errors() {
    assertThat(configure("src/main/conf/logback.xml")).isEmpty();
  }

  /**
   * @return the non-INFO statuses, rendered, so a failure names the offending element
   */
  private List<String> configure(String path) {
    File file = new File(path);
    assertThat(file).exists();
    System.setProperty("OPAL_LOG", logs.getRoot().getAbsolutePath());
    LoggerContext context = new LoggerContext();
    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      configurator.doConfigure(file);
      return context.getStatusManager().getCopyOfStatusList().stream()
          .filter(status -> status.getLevel() != Status.INFO)
          .map(Status::toString)
          .collect(Collectors.toList());
    } catch(Exception e) {
      throw new AssertionError("Cannot parse " + path, e);
    } finally {
      context.stop();
      System.clearProperty("OPAL_LOG");
    }
  }
}
