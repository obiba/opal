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
import java.util.function.Function;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * The audit log already says what was submitted and whether it was refused. What it cannot say is how
 * long the R server took, which is what these spans are for - so duration, failure and, for an
 * asynchronous command, the link back to the request that asked for it are what is worth pinning.
 * <p/>
 * An assignment or an aggregation is one operation: the check of what was submitted and the
 * evaluation on the R server are its two halves, and the span that holds them is what makes them
 * readable as one thing.
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
    DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, "meanDS(D$age)")
        .evaluate("dsBase::meanDS(D$age)", () -> null);

    SpanData operation = span("datashield.aggregate");
    assertThat(attributes(operation)).contains(
        org.fest.assertions.data.MapEntry.entry("datashield.action", "AGGREGATE"),
        org.fest.assertions.data.MapEntry.entry("datashield.profile", "default"),
        org.fest.assertions.data.MapEntry.entry("datashield.session.id", "rsession-42"),
        org.fest.assertions.data.MapEntry.entry("datashield.script", "meanDS(D$age)"));
    SpanData eval = span("datashield.eval");
    assertThat(eval.getParentSpanId()).isEqualTo(operation.getSpanContext().getSpanId());
    assertThat(attributes(eval)).contains(
        org.fest.assertions.data.MapEntry.entry("datashield.action", "AGGREGATE"),
        org.fest.assertions.data.MapEntry.entry("datashield.script", "dsBase::meanDS(D$age)"));
  }

  @Test
  public void test_an_assignment_records_the_symbol_on_the_operation_and_on_the_evaluation() {
    DataShieldTracer.begin(context(), DataShieldLog.Action.ASSIGN, "D", "cbind(x)").evaluate("base::cbind(x)", () -> null);

    assertThat(attributes(span("datashield.assign")).get("datashield.symbol")).isEqualTo("D");
    assertThat(attributes(span("datashield.eval")).get("datashield.symbol")).isEqualTo("D");
  }

  @Test
  public void test_a_failing_evaluation_fails_the_operation() {
    try {
      DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, "boom()").evaluate("boom()", () -> {
        throw new IllegalStateException("disclosure risk");
      });
      throw new AssertionError("the failure should have propagated");
    } catch(IllegalStateException expected) {
      // the operation's exception must reach the caller unchanged
    }

    for(String name : new String[] { "datashield.aggregate", "datashield.eval" }) {
      SpanData span = span(name);
      assertThat(span.getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
      assertThat(span.getStatus().getDescription()).isEqualTo("disclosure risk");
      assertThat(span.getEvents()).isNotEmpty();
    }
  }

  /**
   * An asynchronous DataSHIELD command runs on the R session's consumer thread, where no request
   * context is left. The operation was begun on the request thread and is what the evaluation hangs
   * under, so the thread does not matter - which is the whole point of keying the trace on the session.
   */
  @Test
  public void test_an_evaluation_on_another_thread_stays_under_its_operation() throws Exception {
    Span session = openSession("rsession-42");
    DataShieldTracer.Operation operation =
        DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, "meanDS(D$age)");

    ExecutorService rConsumerThread = Executors.newSingleThreadExecutor();
    try {
      rConsumerThread.submit(() -> operation.evaluate("dsBase::meanDS(D$age)", () -> null)).get();
    } finally {
      rConsumerThread.shutdown();
      rConsumerThread.awaitTermination(5, TimeUnit.SECONDS);
    }

    SpanData aggregate = span("datashield.aggregate");
    SpanData eval = span("datashield.eval");
    assertThat(aggregate.getTraceId()).isEqualTo(session.getSpanContext().getTraceId());
    assertThat(aggregate.getParentSpanId()).isEqualTo(session.getSpanContext().getSpanId());
    assertThat(eval.getTraceId()).isEqualTo(session.getSpanContext().getTraceId());
    assertThat(eval.getParentSpanId()).isEqualTo(aggregate.getSpanContext().getSpanId());
  }

  /**
   * Parsing happens on the request thread, before the R command it produces is queued. It is the
   * first child of the operation, and carries both what was submitted and what was made of it.
   */
  @Test
  public void test_a_parse_is_the_first_half_of_its_operation() throws Exception {
    openSession("rsession-42");
    DataShieldTracer.Operation operation =
        DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, "colnamesDS(\"x\")");

    operation.parse("colnamesDS(\"x\")", () -> "dsBase::colnamesDS(\"x\")", Function.identity());
    operation.evaluate("dsBase::colnamesDS(\"x\")", () -> null);

    SpanData aggregate = span("datashield.aggregate");
    SpanData parse = span("datashield.parse");
    SpanData eval = span("datashield.eval");
    assertThat(parse.getParentSpanId()).isEqualTo(aggregate.getSpanContext().getSpanId());
    assertThat(eval.getParentSpanId()).isEqualTo(aggregate.getSpanContext().getSpanId());
    assertThat(attributes(parse).get("datashield.action")).isEqualTo("PARSE");
    assertThat(attributes(parse).get("datashield.script")).isEqualTo("colnamesDS(\"x\")");
    assertThat(attributes(parse).get("datashield.script.generated")).isEqualTo("dsBase::colnamesDS(\"x\")");
    // the queue wait is what lies between the two halves: the evaluation starts after the check ends
    assertThat(eval.getStartEpochNanos()).isGreaterThanOrEqualTo(parse.getEndEpochNanos());
    assertThat(aggregate.getEndEpochNanos()).isGreaterThanOrEqualTo(eval.getEndEpochNanos());
  }

  /**
   * A script the parser turns down is the restriction doing its job, and the reason an auditor opens
   * the trace at all. Nothing is evaluated, and the operation is over.
   */
  @Test
  public void test_a_refused_script_fails_the_operation_and_ends_it() {
    openSession("rsession-42");
    DataShieldTracer.Operation operation =
        DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, "system(\"rm -rf /\")");

    try {
      operation.parse("system(\"rm -rf /\")", () -> {
        throw new ParseException("system is not an allowed function");
      }, Function.identity());
      throw new AssertionError("the parse failure should have propagated");
    } catch(ParseException expected) {
      // the checked failure must reach the caller unchanged
    }

    assertThat(exporter.getFinishedSpanItems()).hasSize(2);
    for(String name : new String[] { "datashield.aggregate", "datashield.parse" }) {
      assertThat(span(name).getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
      assertThat(span(name).getStatus().getDescription()).isEqualTo("system is not an allowed function");
    }
    assertThat(operation.isEnded()).isTrue();
    assertThat(DataShieldSessionTraces.openOperationCount("rsession-42")).isEqualTo(0);
  }

  /**
   * A table or a resource is not parsed but looked up: it has to exist and the user has to be allowed
   * to read it. The lookup is the check of that operation, under its own name, and a refusal is the
   * operation's refusal.
   */
  @Test
  public void test_a_table_that_cannot_be_resolved_is_refused_before_anything_runs() {
    openSession("rsession-42");
    DataShieldTracer.Operation operation =
        DataShieldTracer.begin(context(), DataShieldLog.Action.ASSIGN, "D", "D <- opal[CNSIM.SECRET]");

    try {
      operation.resolve("datashield.table", "CNSIM.SECRET", () -> {
        throw new IllegalStateException("No such table");
      });
      throw new AssertionError("the refusal should have propagated");
    } catch(IllegalStateException expected) {
      // the lookup's exception must reach the REST layer unchanged
    }

    SpanData resolve = span("datashield.resolve");
    assertThat(resolve.getParentSpanId()).isEqualTo(span("datashield.assign").getSpanContext().getSpanId());
    assertThat(attributes(resolve).get("datashield.action")).isEqualTo("RESOLVE");
    assertThat(attributes(resolve).get("datashield.table")).isEqualTo("CNSIM.SECRET");
    assertThat(resolve.getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
    assertThat(span("datashield.assign").getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
    assertThat(operation.isEnded()).isTrue();
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
  public void test_nothing_breaks_when_no_sdk_is_installed() throws Exception {
    sdk.close();
    sdk = null;
    GlobalOpenTelemetry.resetForTest();

    DataShieldTracer.Operation operation =
        DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, "meanDS(D$age)");
    operation.parse("meanDS(D$age)", () -> "dsBase::meanDS(D$age)", Function.identity());
    Object result = operation.evaluate("dsBase::meanDS(D$age)", () -> "evaluated");

    assertThat(result).isEqualTo("evaluated");
    assertThat(exporter.getFinishedSpanItems()).isEmpty();
    assertThat(DataShieldSessionTraces.openOperationCount("rsession-42")).isEqualTo(0);
  }

  private DataShieldContext context() {
    return new DataShieldContext(null, "rsession-42", "default", "v2", Map.of());
  }

  private SpanData onlySpan() {
    assertThat(exporter.getFinishedSpanItems()).hasSize(1);
    return exporter.getFinishedSpanItems().get(0);
  }

  private SpanData span(String name) {
    return exporter.getFinishedSpanItems().stream().filter(s -> name.equals(s.getName())).findFirst()
        .orElseThrow(() -> new AssertionError("no span named " + name + " among " + exporter.getFinishedSpanItems()));
  }

  private Map<String, String> attributes(SpanData span) {
    return span.getAttributes().asMap().entrySet().stream()
        .collect(java.util.stream.Collectors.toMap(e -> e.getKey().getKey(), e -> String.valueOf(e.getValue())));
  }
}
