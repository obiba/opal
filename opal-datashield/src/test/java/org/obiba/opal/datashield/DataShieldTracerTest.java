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
import org.obiba.datashield.r.expr.ParseException;
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
    DataShieldSessionTraces.endAll();
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
   * An asynchronous DataSHIELD command runs on the R session's consumer thread, where no request
   * context is left. The session id is what the context is looked up by, so the thread does not
   * matter - which is the whole point of keying the trace on the session.
   */
  @Test
  public void test_a_span_opened_on_another_thread_stays_in_the_sessions_trace() throws Exception {
    Span session = openSession("rsession-42");
    DataShieldContext context = context();

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
    assertThat(aggregate.getTraceId()).isEqualTo(session.getSpanContext().getTraceId());
    assertThat(aggregate.getParentSpanId()).isEqualTo(session.getSpanContext().getSpanId());
  }

  /**
   * Parsing happens on the request thread, before the R command it produces is queued, so it is a
   * sibling of the evaluation rather than its parent - as the audit log records it.
   */
  @Test
  public void test_a_parse_is_traced_in_the_sessions_trace() throws Exception {
    Span session = openSession("rsession-42");

    DataShieldTracer.tracedParse(context(), "colnamesDS(\"x\")", () -> "dsBase::colnamesDS(\"x\")");

    SpanData parse = onlySpan();
    assertThat(parse.getName()).isEqualTo("datashield.parse");
    assertThat(parse.getTraceId()).isEqualTo(session.getSpanContext().getTraceId());
    assertThat(parse.getParentSpanId()).isEqualTo(session.getSpanContext().getSpanId());
    assertThat(attributes(parse).get("datashield.script")).isEqualTo("colnamesDS(\"x\")");
  }

  /**
   * A script the parser turns down is the restriction doing its job, and the reason an auditor opens
   * the trace at all.
   */
  @Test
  public void test_a_refused_script_is_reported_as_an_error() {
    openSession("rsession-42");

    try {
      DataShieldTracer.tracedParse(context(), "system(\"rm -rf /\")", () -> {
        throw new ParseException("system is not an allowed function");
      });
      throw new AssertionError("the parse failure should have propagated");
    } catch(ParseException expected) {
      // the checked failure must reach the caller unchanged
    }

    SpanData parse = onlySpan();
    assertThat(parse.getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
    assertThat(parse.getStatus().getDescription()).isEqualTo("system is not an allowed function");
  }

  @Test
  public void test_the_session_id_can_be_added_once_the_session_exists() {
    DataShieldTracer.traced(null, "default", DataShieldLog.Action.OPEN, () -> {
      DataShieldTracer.describeCurrentSession("rsession-99");
      return null;
    });

    assertThat(attributes(onlySpan()).get("datashield.session.id")).isEqualTo("rsession-99");
  }

  /**
   * Opens a session trace the way the REST resource does, and hands back its span so that the trace
   * the operations are expected to join can be named.
   */
  private Span openSession(String rid) {
    DataShieldSessionTraces.opening(() -> {
      DataShieldSessionTraces.bind(rid, "default");
      return null;
    });
    Span session = Span.fromContext(DataShieldSessionTraces.contextOf(rid));
    exporter.reset();
    return session;
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
