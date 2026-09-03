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

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * The DataSHIELD audit stream as an OTLP consumer sees it. Two things are being pinned: that the
 * records reach the exporter at all - they did not before, because nothing installed an SDK into the
 * appender - and that the MDC keys arrive under their OpenTelemetry names rather than the ds_* ones
 * the log file uses.
 */
public class DatashieldOtelExportTest extends AbstractDatashieldLoggingTest {

  private InMemoryLogRecordExporter exporter;

  private OpenTelemetrySdk sdk;

  @Before
  public void installSdk() {
    exporter = InMemoryLogRecordExporter.create();
    sdk = OpenTelemetrySdk.builder()
        .setLoggerProvider(SdkLoggerProvider.builder()
            .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
            .build())
        .build();
    // the same call OpalServer makes at startup; it has to find the appender nested inside
    // MdcRenamingAppender, which it does by walking AppenderAttachable
    OpenTelemetryAppender.install(sdk);
  }

  @After
  public void closeSdk() {
    if(sdk != null) sdk.close();
  }

  @Test
  public void test_the_audit_record_reaches_the_exporter() {
    logDatashieldEvent();

    assertThat(exporter.getFinishedLogRecordItems()).hasSize(1);
    LogRecordData record = exporter.getFinishedLogRecordItems().iterator().next();
    assertThat(record.getBodyValue().asString()).isEqualTo("evaluated '" + SCRIPT + "'");
    assertThat(record.getInstrumentationScopeInfo().getName()).isEqualTo("datashield.user");
  }

  @Test
  public void test_the_mdc_keys_are_exported_under_their_opentelemetry_names() {
    logDatashieldEvent();

    Map<String, String> attributes = exportedAttributes();

    assertThat(attributes.get("datashield.session.id")).isEqualTo("rsession-42");
    assertThat(attributes.get("datashield.profile")).isEqualTo("default");
    assertThat(attributes.get("datashield.action")).isEqualTo("AGGREGATE");
    assertThat(attributes.get("datashield.symbol")).isEqualTo("D");
    assertThat(attributes.get("enduser.id")).isEqualTo("administrator");
    assertThat(attributes.get("client.address")).isEqualTo("10.0.0.7");
  }

  /**
   * The submitted expression is the substance of the audit trail: disclosure attempts show up in the
   * expression itself, so it is exported whole rather than dropped or capped.
   */
  @Test
  public void test_the_submitted_script_is_exported_in_full() {
    logDatashieldEvent();

    assertThat(exportedAttributes().get("datashield.script")).isEqualTo(SCRIPT);
  }

  @Test
  public void test_no_raw_mdc_key_leaks_into_the_exported_attributes() {
    logDatashieldEvent();

    assertThat(exportedAttributes().keySet())
        .doesNotContain("ds_id", "ds_profile", "ds_action", "ds_symbol", "ds_eval", "username", "ip");
  }

  /**
   * Parsing a submitted expression logs three more MDC keys than the other actions do, and they are
   * exported like the rest - so they need renaming like the rest.
   */
  @Test
  public void test_the_parse_record_exports_the_submitted_and_generated_scripts() {
    logParseEvent();

    Map<String, String> attributes = exportedAttributes();

    assertThat(attributes.get("datashield.script.submitted")).isEqualTo("meanDS(D$age)");
    assertThat(attributes.get("datashield.script.generated")).isEqualTo("base::mean(D$age)");
    assertThat(attributes.get("datashield.script.mapping")).isEqualTo("meanDS=base::mean");
    assertThat(attributes.keySet()).doesNotContain("ds_script_in", "ds_script_out", "ds_map");
  }

  private Map<String, String> exportedAttributes() {
    assertThat(exporter.getFinishedLogRecordItems()).isNotEmpty();
    return exporter.getFinishedLogRecordItems().iterator().next().getAttributes().asMap().entrySet()
        .stream()
        .collect(Collectors.toMap(e -> ((AttributeKey<?>) e.getKey()).getKey(), e -> String.valueOf(e.getValue())));
  }
}
