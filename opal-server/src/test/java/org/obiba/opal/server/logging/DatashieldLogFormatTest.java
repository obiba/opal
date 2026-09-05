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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * datashield.log is parsed by tooling outside Opal, so its field names, their values and their order
 * are a published interface. The OpenTelemetry work renames these keys on the way to the exporter -
 * see MdcRenamingAppender - precisely so that this file does not change; this test is what keeps that
 * true. If it fails, a change has leaked a renamed, added or removed MDC key into the file, and the
 * fix is to move that change into the export branch rather than to update the fixture.
 */
public class DatashieldLogFormatTest extends AbstractDatashieldLoggingTest {

  /**
   * Not compared: the timestamp, and the thread name, which is the test runner's rather than a
   * request thread's. Their field names are still checked, in position.
   */
  private static final List<String> VOLATILE_FIELDS = List.of("@timestamp", "thread_name");

  @Test
  public void test_the_datashield_log_line_is_unchanged() throws Exception {
    logDatashieldEvent();

    Map<String, Object> actual = readLogLine();
    Map<String, Object> expected = readFixture();

    assertThat(new ArrayList<>(actual.keySet()))
        .as("datashield.log field names, in order")
        .isEqualTo(new ArrayList<>(expected.keySet()));

    VOLATILE_FIELDS.forEach(field -> {
      actual.remove(field);
      expected.remove(field);
    });
    assertThat(actual).as("datashield.log field values").isEqualTo(expected);
  }

  @Test
  public void test_the_submitted_script_is_written_to_the_file() throws Exception {
    logDatashieldEvent();

    assertThat(readLogLine().get("ds_eval")).isEqualTo(SCRIPT);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> readLogLine() throws Exception {
    File log = new File(System.getProperty("OPAL_LOG"), "datashield.log");
    assertThat(log).exists();
    List<String> lines = java.nio.file.Files.readAllLines(log.toPath());
    assertThat(lines).hasSize(1);
    return new ObjectMapper().readValue(lines.get(0), java.util.LinkedHashMap.class);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> readFixture() throws Exception {
    return new ObjectMapper()
        .readValue(getClass().getResourceAsStream("/datashield-log-format.json"), java.util.LinkedHashMap.class);
  }
}
