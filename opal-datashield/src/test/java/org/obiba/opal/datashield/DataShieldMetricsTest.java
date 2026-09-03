/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield;

import com.google.common.collect.Lists;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RServerProfile;
import org.obiba.opal.r.service.RServerSession;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Metric attributes multiply the time series a backend keeps, so what is deliberately absent here
 * matters as much as what is present: no session id, no user, no submitted expression, and no symbol
 * - a symbol is named by the user and so is unbounded.
 */
public class DataShieldMetricsTest {

  private InMemoryMetricReader reader;

  private OpenTelemetrySdk sdk;

  @Before
  public void installSdk() {
    GlobalOpenTelemetry.resetForTest();
    reader = InMemoryMetricReader.create();
    sdk = OpenTelemetrySdk.builder()
        .setMeterProvider(SdkMeterProvider.builder().registerMetricReader(reader).build())
        .build();
    GlobalOpenTelemetry.set(sdk);
  }

  @After
  public void removeSdk() {
    if(sdk != null) sdk.close();
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  public void test_a_successful_operation_is_counted_and_timed() {
    DataShieldTracer.traced(context(), DataShieldLog.Action.AGGREGATE, null, "meanDS(D$age)", () -> null);

    assertThat(pointCount("datashield.operation.count")).isEqualTo(1);
    assertThat(attributesOf("datashield.operation.count")).isEqualTo(
        Map.of("datashield.action", "AGGREGATE", "datashield.profile", "default", "datashield.outcome", "ok"));
    assertThat(pointCount("datashield.operation.duration")).isEqualTo(1);
  }

  @Test
  public void test_a_failing_operation_is_counted_as_an_error() {
    try {
      DataShieldTracer.traced(context(), DataShieldLog.Action.ASSIGN, "D", "boom()", () -> {
        throw new IllegalStateException("disclosure risk");
      });
    } catch(IllegalStateException expected) {
      // recorded, then rethrown
    }

    assertThat(attributesOf("datashield.operation.count").get("datashield.outcome")).isEqualTo("error");
  }

  @Test
  public void test_the_operation_metrics_carry_no_unbounded_attribute() {
    DataShieldTracer.traced(context(), DataShieldLog.Action.ASSIGN, "someUserChosenSymbol", "cbind(x)", () -> null);

    Set<String> keys = attributesOf("datashield.operation.count").keySet();
    assertThat(keys).doesNotContain("datashield.session.id", "datashield.symbol", "datashield.script", "enduser.id");
  }

  @Test
  public void test_a_refused_session_is_counted_per_quota_metric() {
    DataShieldMetrics.recordQuotaRejection("SESSION_TIME");

    assertThat(attributesOf("datashield.quota.rejection"))
        .isEqualTo(Map.of("datashield.quota.metric", "SESSION_TIME"));
  }

  /**
   * Sessions also end by timing out, so the gauge asks the manager on collection instead of being
   * maintained around the REST endpoints, where those endings are invisible.
   */
  @Test
  public void test_open_sessions_are_observed_by_profile() throws Exception {
    // built before the manager is stubbed: stubbing a mock inside another when() confuses Mockito
    List<RServerSession> sessions = Lists.newArrayList(
        session("DataSHIELD", "default"),
        session("DataSHIELD", "default"),
        session("DataSHIELD", "survival"),
        session("R", "default"));            // a plain R session, not ours
    OpalRSessionManager manager = mock(OpalRSessionManager.class);
    when(manager.getRSessions()).thenReturn(sessions);
    DataShieldSessionMetrics metrics = new DataShieldSessionMetrics(manager);
    metrics.register();
    try {
      Map<String, Long> byProfile = reader.collectAllMetrics().stream()
          .filter(m -> "datashield.session.active".equals(m.getName()))
          .flatMap(m -> m.getLongGaugeData().getPoints().stream())
          .collect(Collectors.toMap(p -> p.getAttributes().get(
              io.opentelemetry.api.common.AttributeKey.stringKey("datashield.profile")), p -> p.getValue()));

      assertThat(byProfile).isEqualTo(Map.of("default", 2L, "survival", 1L));
    } finally {
      metrics.unregister();
    }
  }

  private RServerSession session(String executionContext, String profileName) {
    RServerProfile profile = mock(RServerProfile.class);
    when(profile.getName()).thenReturn(profileName);
    RServerSession session = mock(RServerSession.class);
    when(session.getExecutionContext()).thenReturn(executionContext);
    when(session.getProfile()).thenReturn(profile);
    return session;
  }

  private DataShieldContext context() {
    return new DataShieldContext(null, "rsession-42", "default", "v2", Map.of());
  }

  private MetricData metric(String name) {
    return reader.collectAllMetrics().stream().filter(m -> name.equals(m.getName())).findFirst()
        .orElseThrow(() -> new AssertionError("no metric named " + name));
  }

  private Collection<? extends PointData> points(String name) {
    MetricData data = metric(name);
    return data.getData().getPoints();
  }

  private long pointCount(String name) {
    return points(name).size();
  }

  private Map<String, String> attributesOf(String name) {
    return points(name).iterator().next().getAttributes().asMap().entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey().getKey(), e -> String.valueOf(e.getValue())));
  }
}
