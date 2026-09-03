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

import org.junit.After;
import org.junit.Test;

import java.util.Map;
import java.util.function.UnaryOperator;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * OpenTelemetry export is opt-in, because an autoconfigured SDK defaults to OTLP on localhost and an
 * installation that never asked for telemetry would just log connection failures. The endpoint can be
 * configured globally or per signal, and either spelling means the operator asked for it - a gate
 * that only recognised OTEL_EXPORTER_OTLP_ENDPOINT would silently export nothing for the other.
 */
public class OpalServerTest {

  private static final UnaryOperator<String> NO_ENV = name -> null;

  @After
  public void clearSystemProperties() {
    System.clearProperty("otel.exporter.otlp.endpoint");
    System.clearProperty("otel.exporter.otlp.logs.endpoint");
  }

  @Test
  public void test_telemetry_is_off_when_no_endpoint_is_configured() {
    assertThat(OpalServer.hasOtlpEndpoint(NO_ENV)).isFalse();
  }

  @Test
  public void test_an_empty_endpoint_does_not_count_as_configured() {
    assertThat(OpalServer.hasOtlpEndpoint(env("OTEL_EXPORTER_OTLP_ENDPOINT", ""))).isFalse();
  }

  @Test
  public void test_the_global_endpoint_environment_variable_enables_telemetry() {
    assertThat(OpalServer.hasOtlpEndpoint(env("OTEL_EXPORTER_OTLP_ENDPOINT", "https://collector:4318"))).isTrue();
  }

  @Test
  public void test_the_logs_specific_endpoint_environment_variable_enables_telemetry() {
    assertThat(OpalServer.hasOtlpEndpoint(env("OTEL_EXPORTER_OTLP_LOGS_ENDPOINT", "https://collector:4318/v1/logs")))
        .isTrue();
  }

  @Test
  public void test_the_global_endpoint_system_property_enables_telemetry() {
    System.setProperty("otel.exporter.otlp.endpoint", "https://collector:4318");
    assertThat(OpalServer.hasOtlpEndpoint(NO_ENV)).isTrue();
  }

  @Test
  public void test_the_logs_specific_endpoint_system_property_enables_telemetry() {
    System.setProperty("otel.exporter.otlp.logs.endpoint", "https://collector:4318/v1/logs");
    assertThat(OpalServer.hasOtlpEndpoint(NO_ENV)).isTrue();
  }

  private UnaryOperator<String> env(String name, String value) {
    Map<String, String> env = Map.of(name, value);
    return env::get;
  }
}
