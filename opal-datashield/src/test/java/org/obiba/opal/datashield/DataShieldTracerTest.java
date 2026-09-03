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

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * The audit log already says what was submitted and whether it was refused. What it cannot say is how
 * long the R server took, which is what these spans are for - so duration, failure and, for an
 * asynchronous command, the link back to the request that asked for it are what is worth pinning.
 */
public class DataShieldTracerTest {

  private InMemorySpanExporter exporter;

  private OpenTelemetrySdk sdk;

  @Before
  public void installSdk() {
    GlobalOpenTelemetry.resetForTest();
    exporter = InMemorySpanExporter.create();
    sdk = OpenTelemetrySdk.builder()
        .setTracerProvider(SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build())
        .build();
    GlobalOpenTelemetry.set(sdk);
  }

  @After
  public void removeSdk() {
    if(sdk != null) sdk.close();
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  public void test_an_aggregation_is_traced_with_the_exported_attribute_names() {
    DataShieldTracer.traced(context(), DataShieldLog.Action.AGGREGATE, null, "meanDS(D$age)", () -> null);

    SpanData span = onlySpan();
    assertThat(span.getName()).isEqualTo("datashield.aggregate");
    assertThat(attributes(span)).contains(
        org.fest.assertions.data.MapEntry.entry("datashield.action", "AGGREGATE"),
        org.fest.assertions.data.MapEntry.entry("datashield.profile", "default"),
        org.fest.assertions.data.MapEntry.entry("datashield.session.id", "rsession-42"),
        org.fest.assertions.data.MapEntry.entry("datashield.script", "meanDS(D$age)"));
  }

  @Test
  public void test_an_assignment_records_the_symbol() {
    DataShieldTracer.traced(context(), DataShieldLog.Action.ASSIGN, "D", "cbind(x)", () -> null);

    assertThat(onlySpan().getName()).isEqualTo("datashield.assign");
    assertThat(attributes(onlySpan()).get("datashield.symbol")).isEqualTo("D");
  }

  @Test
  public void test_a_failing_operation_is_reported_as_an_error() {
    try {
      DataShieldTracer.traced(context(), DataShieldLog.Action.AGGREGATE, null, "boom()", () -> {
        throw new IllegalStateException("disclosure risk");
      });
      throw new AssertionError("the failure should have propagated");
    } catch(IllegalStateException expected) {
      // the operation's exception must reach the caller unchanged
    }

    SpanData span = onlySpan();
    assertThat(span.getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
    assertThat(span.getStatus().getDescription()).isEqualTo("disclosure risk");
    assertThat(span.getEvents()).isNotEmpty();
  }

  /**
   * An asynchronous DataSHIELD command runs on the R session's consumer thread, where the request's
   * trace context is gone. DataShieldContext captures it at construction so the span still lands in
   * the right trace.
   */
  @Test
  public void test_a_span_opened_on_another_thread_stays_in_the_requests_trace() throws Exception {
    Span request = GlobalOpenTelemetry.getTracer("test").spanBuilder("PUT /datashield/session").startSpan();
    DataShieldContext context;
    try(Scope ignored = request.makeCurrent()) {
      context = context();
    }
    request.end();

    ExecutorService rConsumerThread = Executors.newSingleThreadExecutor();
    try {
      rConsumerThread.submit(() ->
          DataShieldTracer.traced(context, DataShieldLog.Action.AGGREGATE, null, "meanDS(D$age)", () -> null)).get();
    } finally {
      rConsumerThread.shutdown();
      rConsumerThread.awaitTermination(5, TimeUnit.SECONDS);
    }

    SpanData aggregate = exporter.getFinishedSpanItems().stream()
        .filter(s -> "datashield.aggregate".equals(s.getName())).findFirst().orElseThrow();
    assertThat(aggregate.getTraceId()).isEqualTo(request.getSpanContext().getTraceId());
    assertThat(aggregate.getParentSpanId()).isEqualTo(request.getSpanContext().getSpanId());
  }

  @Test
  public void test_the_session_id_can_be_added_once_the_session_exists() {
    DataShieldTracer.traced("default", DataShieldLog.Action.OPEN, () -> {
      DataShieldTracer.describeCurrentSession("rsession-99");
      return null;
    });

    assertThat(attributes(onlySpan()).get("datashield.session.id")).isEqualTo("rsession-99");
  }

  /**
   * Telemetry is opt-in, so on most installations no SDK is ever built. The instrumentation still has
   * to run, and cost nothing.
   */
  @Test
  public void test_nothing_breaks_when_no_sdk_is_installed() {
    sdk.close();
    sdk = null;
    GlobalOpenTelemetry.resetForTest();

    Object result = DataShieldTracer.traced(context(), DataShieldLog.Action.AGGREGATE, null, "meanDS(D$age)",
        () -> "evaluated");

    assertThat(result).isEqualTo("evaluated");
    assertThat(exporter.getFinishedSpanItems()).isEmpty();
  }

  private DataShieldContext context() {
    return new DataShieldContext(null, "rsession-42", "default", "v2", Map.of());
  }

  private SpanData onlySpan() {
    assertThat(exporter.getFinishedSpanItems()).hasSize(1);
    return exporter.getFinishedSpanItems().get(0);
  }

  private Map<String, String> attributes(SpanData span) {
    return span.getAttributes().asMap().entrySet().stream()
        .collect(java.util.stream.Collectors.toMap(e -> e.getKey().getKey(), e -> String.valueOf(e.getValue())));
  }
}
