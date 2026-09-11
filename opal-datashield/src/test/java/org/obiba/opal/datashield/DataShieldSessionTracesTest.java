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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * One DataSHIELD session is one trace. A session is a sequence of requests - open, assign, aggregate,
 * close - which the audit log ties together by its id and nothing else does, so the trace has to be
 * tied together the same way. Anything less produces a trace per operation, which repeats what a log
 * line already says. Under the session, each assignment and aggregation is a subtree of its own: the
 * check of what was submitted, then its evaluation.
 */
public class DataShieldSessionTracesTest {

  private static final String RID = "22c3b73f-7e64-4dae-bf46-02d84ed3b4a2";

  private InMemorySpanExporter exporter;

  private OpenTelemetrySdk sdk;

  private AppenderBase<ILoggingEvent> auditAppender;

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
    detachAuditAppender();
    DataShieldSessionTraces.endAll();
    if(sdk != null) sdk.close();
    GlobalOpenTelemetry.resetForTest();
  }

  /**
   * The session of the documented audit trail, played back in order: it has to come out as one trace
   * rooted on the session, the four operations under it, and under the assignment and the aggregation
   * their check and their evaluation.
   */
  @Test
  public void test_a_whole_session_is_one_tree_rooted_on_the_session() throws Exception {
    open();
    assignTable("x", "CNSIM.CNSIM1");
    aggregate("colnamesDS(\"x\")", "dsBase::colnamesDS(\"x\")");
    close();

    List<SpanData> spans = exporter.getFinishedSpanItems();
    assertThat(spans.stream().map(SpanData::getName).collect(java.util.stream.Collectors.toList())).isEqualTo(List.of(
        "datashield.open",
        "datashield.resolve", "datashield.eval", "datashield.assign",
        "datashield.parse", "datashield.eval", "datashield.aggregate",
        "datashield.close", "datashield.session"));

    SpanData session = byName().get("datashield.session");
    assertThat(session.getParentSpanId()).isEqualTo("0000000000000000");
    spans.forEach(span -> assertThat(span.getTraceId()).isEqualTo(session.getTraceId()));
    for(String operation : List.of("datashield.open", "datashield.assign", "datashield.aggregate", "datashield.close")) {
      assertThat(byName().get(operation).getParentSpanId()).isEqualTo(session.getSpanContext().getSpanId());
    }
    String assign = byName().get("datashield.assign").getSpanContext().getSpanId();
    String aggregate = byName().get("datashield.aggregate").getSpanContext().getSpanId();
    assertThat(spans.get(1).getParentSpanId()).isEqualTo(assign);      // resolve
    assertThat(spans.get(2).getParentSpanId()).isEqualTo(assign);      // eval
    assertThat(spans.get(4).getParentSpanId()).isEqualTo(aggregate);   // parse
    assertThat(spans.get(5).getParentSpanId()).isEqualTo(aggregate);   // eval
  }

  /**
   * A command is checked on the request thread and queued; the session may close, expire or lose its
   * R server before the consumer gets to it, and then the queue is dropped. The operation's span would
   * stay open forever - and its check, already exported, would point at a parent that never arrives.
   */
  @Test
  public void test_an_operation_never_run_is_ended_with_its_session() throws Exception {
    open();
    DataShieldTracer.Operation queued =
        DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, "colnamesDS(\"x\")");
    queued.parse("colnamesDS(\"x\")", () -> "dsBase::colnamesDS(\"x\")", Function.identity());
    assertThat(DataShieldSessionTraces.openOperationCount(RID)).isEqualTo(1);

    close();

    SpanData aggregate = byName().get("datashield.aggregate");
    assertThat(aggregate.getStatus().getStatusCode()).isEqualTo(io.opentelemetry.api.trace.StatusCode.ERROR);
    assertThat(aggregate.getStatus().getDescription()).isEqualTo("session ended before the command ran");
    assertThat(aggregate.getParentSpanId()).isEqualTo(byName().get("datashield.session").getSpanContext().getSpanId());
    assertThat(DataShieldSessionTraces.openOperationCount(RID)).isEqualTo(0);
    assertThat(queued.isEnded()).isTrue();
  }

  @Test
  public void test_an_operation_never_run_is_ended_by_the_reaper() {
    open();
    DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, "colnamesDS(\"x\")");

    DataShieldSessionTraces.retain(Set.of("some-other-session"));

    assertThat(byName()).containsKey("datashield.aggregate");
    assertThat(byName()).containsKey("datashield.session");
    assertThat(DataShieldSessionTraces.openOperationCount(RID)).isEqualTo(0);
  }

  /**
   * An operation of a session that was never traced is a trace of its own, and so is its straggler:
   * the reaper ends it like any other, keyed on the session id it was begun with.
   */
  @Test
  public void test_a_straggler_of_an_untraced_session_is_ended_by_the_reaper() {
    DataShieldTracer.begin(new DataShieldContext(null, "never-opened", "default", "v2", Map.of()),
        DataShieldLog.Action.AGGREGATE, null, "meanDS(D$age)");

    DataShieldSessionTraces.retain(Set.of());

    assertThat(byName().get("datashield.aggregate").getParentSpanId()).isEqualTo("0000000000000000");
    assertThat(DataShieldSessionTraces.openOperationCount("never-opened")).isEqualTo(0);
  }

  /**
   * The trace is searched by what the log file carries: the session id, and the two questions an
   * auditor starts from - who, and from where.
   */
  @Test
  public void test_the_root_span_carries_the_session_id_the_profile_and_the_user() {
    login();
    MDC.put("ip", "172.26.0.1");
    try {
      open();
    } finally {
      MDC.remove("ip");
    }
    close();

    Map<String, String> attributes = byName().get("datashield.session").getAttributes().asMap().entrySet()
        .stream().collect(java.util.stream.Collectors.toMap(e -> e.getKey().getKey(), e -> String.valueOf(e.getValue())));
    assertThat(attributes.get("datashield.session.id")).isEqualTo(RID);
    assertThat(attributes.get("datashield.profile")).isEqualTo("default");
    assertThat(attributes.get("enduser.id")).isEqualTo("dsuser");
    assertThat(attributes.get("client.address")).isEqualTo("172.26.0.1");
  }

  /**
   * The root is only exported when the session ends, so an operation must not have to wait for it:
   * the trace is readable while the session is still open, which is when an auditor watching a
   * suspect session most wants to read it.
   */
  @Test
  public void test_the_operations_are_exported_before_the_session_ends() {
    open();
    aggregate("dsBase::colnamesDS(\"x\")");

    assertThat(byName().keySet()).doesNotContain("datashield.session");
    assertThat(byName().keySet()).contains("datashield.aggregate");
  }

  /**
   * Sessions expire, R servers go away, Opal restarts. The span of a session that ends any other way
   * than through its CLOSE endpoint would stay open, and an open span is never exported - the whole
   * trace would be lost, not only its root.
   */
  @Test
  public void test_a_session_that_is_never_closed_is_ended_when_it_is_gone() {
    open();
    aggregate("dsBase::colnamesDS(\"x\")");

    DataShieldSessionTraces.retain(Set.of("some-other-session"));

    assertThat(byName()).containsKey("datashield.session");
    assertThat(DataShieldSessionTraces.openTraceCount()).isEqualTo(0);
  }

  /**
   * The reaper lists the open traces first and asks the manager second. A session opened in between
   * is bound after the listing, so it is not a candidate - whatever the manager's answer says about
   * it. The other order would end the trace of a session that has only just started.
   */
  @Test
  public void test_a_session_opened_while_the_reaper_runs_is_left_alone() {
    DataShieldSessionTraces.retain(() -> {
      open();
      return Set.of();
    });

    assertThat(DataShieldSessionTraces.openTraceCount()).isEqualTo(1);
    assertThat(byName()).doesNotContainKey("datashield.session");
  }

  @Test
  public void test_a_session_still_held_is_left_alone_by_the_reaper() {
    open();

    DataShieldSessionTraces.retain(Set.of(RID));

    assertThat(byName()).doesNotContainKey("datashield.session");
    assertThat(DataShieldSessionTraces.openTraceCount()).isEqualTo(1);
  }

  /**
   * A session refused by the quota gate, or one the R server fails to start, never gets an id - so
   * nothing will ever close its span, and it has to be closed on the spot.
   */
  @Test
  public void test_a_session_that_is_never_created_leaves_no_trace_open() {
    try {
      DataShieldSessionTraces.opening(() -> {
        throw new IllegalStateException("DataSHIELD quota exceeded");
      });
      throw new AssertionError("the refusal should have propagated");
    } catch(IllegalStateException expected) {
      // the caller's exception must reach the REST layer unchanged
    }

    assertThat(DataShieldSessionTraces.openTraceCount()).isEqualTo(0);
    assertThat(byName().get("datashield.session").getStatus().getDescription())
        .isEqualTo("DataSHIELD quota exceeded");
  }

  /**
   * Sessions open when telemetry was switched on mid-flight have no trace to join. They are traced
   * one operation at a time, as they were before - not dropped, and not held against a root that
   * will never come.
   */
  @Test
  public void test_an_operation_of_an_unknown_session_is_its_own_trace() {
    DataShieldTracer.begin(new DataShieldContext(null, "never-opened", "default", "v2", Map.of()),
        DataShieldLog.Action.AGGREGATE, null, "meanDS(D$age)").evaluate("dsBase::meanDS(D$age)", () -> null);

    assertThat(byName().get("datashield.aggregate").getParentSpanId()).isEqualTo("0000000000000000");
    assertThat(byName().get("datashield.eval").getParentSpanId())
        .isEqualTo(byName().get("datashield.aggregate").getSpanContext().getSpanId());
  }

  /**
   * The audit log and the spans have to be readable as one thing - a trace whose log lines cannot be
   * found, or log lines whose trace cannot be found, is what makes two useful streams useless. The
   * OpenTelemetry logback appender stamps the trace and span ids of the context that is current when
   * the record is appended, which is what this appender reads back.
   */
  @Test
  public void test_the_audit_record_of_a_session_carries_its_trace_id() {
    login();
    List<SpanContext> appended = attachAuditAppender();
    open();

    DataShieldLog.userLog(context(), DataShieldLog.Action.AGGREGATE, "evaluated '{}'", "dsBase::colnamesDS(\"x\")");

    assertThat(appended).hasSize(1);
    assertThat(appended.get(0).getTraceId())
        .isEqualTo(Span.fromContext(DataShieldSessionTraces.contextOf(RID)).getSpanContext().getTraceId());
  }

  /**
   * Sessions that are not traced still log: the record is written with no trace context rather than
   * with somebody else's.
   */
  @Test
  public void test_the_audit_record_of_an_untraced_session_carries_no_trace_id() {
    login();
    List<SpanContext> appended = attachAuditAppender();

    DataShieldLog.userLog("never-opened", DataShieldLog.Action.AGGREGATE, "evaluated '{}'", "meanDS(D$age)");

    assertThat(appended).hasSize(1);
    assertThat(appended.get(0).isValid()).isFalse();
  }

  /**
   * Nothing instruments Opal's HTTP layer by default, so a DataSHIELD trace normally stands alone.
   * Under the OpenTelemetry Java agent it does not: there is a request span, in a trace of its own,
   * and the two would have no way to find each other. A link is how they do.
   */
  @Test
  public void test_the_session_links_to_the_request_that_opened_it() {
    Span request = GlobalOpenTelemetry.getTracer("test").spanBuilder("POST /ws/datashield/sessions").startSpan();
    try(Scope ignored = request.makeCurrent()) {
      open();
    }
    request.end();
    close();

    SpanData session = byName().get("datashield.session");
    // still a root of its own trace: the session is the trace, the request is only reachable from it
    assertThat(session.getParentSpanId()).isEqualTo("0000000000000000");
    assertThat(session.getTraceId()).isNotEqualTo(request.getSpanContext().getTraceId());
    assertThat(session.getLinks()).hasSize(1);
    assertThat(session.getLinks().get(0).getSpanContext().getSpanId())
        .isEqualTo(request.getSpanContext().getSpanId());
  }

  @Test
  public void test_an_operation_links_to_the_request_that_asked_for_it() {
    open();
    Span request = GlobalOpenTelemetry.getTracer("test").spanBuilder("POST /ws/datashield/session/x/aggregate")
        .startSpan();
    try(Scope ignored = request.makeCurrent()) {
      aggregate("dsBase::colnamesDS(\"x\")");
    }
    request.end();

    SpanData aggregate = byName().get("datashield.aggregate");
    assertThat(aggregate.getTraceId()).isEqualTo(sessionTraceId());
    assertThat(aggregate.getLinks()).hasSize(1);
    assertThat(aggregate.getLinks().get(0).getSpanContext().getSpanId())
        .isEqualTo(request.getSpanContext().getSpanId());
  }

  /**
   * The ordinary case, with nothing instrumenting the HTTP layer: no request span exists, so there
   * is nothing to link and no link is made. A span already inside the session's trace is not linked
   * to it either - the parent relationship already says so.
   */
  @Test
  public void test_no_link_is_made_when_the_request_is_not_traced() {
    open();
    aggregate("dsBase::colnamesDS(\"x\")");
    close();

    assertThat(byName().get("datashield.session").getLinks()).isEmpty();
    assertThat(byName().get("datashield.open").getLinks()).isEmpty();
    assertThat(byName().get("datashield.aggregate").getLinks()).isEmpty();
  }

  /**
   * Under the agent every audit record is written with an HTTP request span current. Following it
   * would scatter one session's records over as many traces as the session had requests, which is
   * the thing the session trace exists to prevent.
   */
  @Test
  public void test_an_audit_record_joins_its_session_trace_from_another_trace() {
    login();
    List<SpanContext> appended = attachAuditAppender();
    open();

    Span request = GlobalOpenTelemetry.getTracer("test").spanBuilder("POST /ws/datashield/session/x/aggregate")
        .startSpan();
    try(Scope ignored = request.makeCurrent()) {
      DataShieldLog.userLog(context(), DataShieldLog.Action.AGGREGATE, "evaluated '{}'", "dsBase::colnamesDS(\"x\")");
    }
    request.end();

    assertThat(appended).hasSize(1);
    assertThat(appended.get(0).getTraceId()).isEqualTo(sessionTraceId());
    assertThat(appended.get(0).getTraceId()).isNotEqualTo(request.getSpanContext().getTraceId());
  }

  /**
   * A record written from inside one of the session's own spans keeps it: same trace either way, and
   * the operation is the more precise of the two anchors. The records of an operation are written
   * with the operation current - before and after its check, before and after its evaluation - and
   * so all land on the operation, not on the session root.
   */
  @Test
  public void test_the_audit_records_of_an_operation_are_anchored_on_it() throws Exception {
    login();
    List<SpanContext> appended = attachAuditAppender();
    open();

    DataShieldTracer.Operation operation =
        DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, "meanDS(D$age)");
    try(Scope ignored = operation.makeCurrent()) {
      operation.parse("meanDS(D$age)", () -> "dsBase::meanDS(D$age)", Function.identity());
      DataShieldLog.userLog(context(), DataShieldLog.Action.PARSE, "parsed '{}'", "dsBase::meanDS(D$age)");
    }
    try(Scope ignored = operation.makeCurrent()) {
      operation.evaluate("dsBase::meanDS(D$age)", () -> null);
      DataShieldLog.userLog(context(), DataShieldLog.Action.AGGREGATE, "evaluated '{}'", "dsBase::meanDS(D$age)");
    }

    assertThat(appended).hasSize(2);
    String aggregate = byName().get("datashield.aggregate").getSpanContext().getSpanId();
    assertThat(appended.get(0).getSpanId()).isEqualTo(aggregate);
    assertThat(appended.get(1).getSpanId()).isEqualTo(aggregate);
  }

  private String sessionTraceId() {
    return Span.fromContext(DataShieldSessionTraces.contextOf(RID)).getSpanContext().getTraceId();
  }

  private void open() {
    DataShieldSessionTraces.opening(() -> DataShieldTracer.traced(null, "default", DataShieldLog.Action.OPEN, () -> {
      DataShieldTracer.describeCurrentSession(RID);
      DataShieldSessionTraces.bind(RID, "default");
      return null;
    }));
  }

  private void assignTable(String symbol, String table) {
    DataShieldTracer.Operation operation =
        DataShieldTracer.begin(context(), DataShieldLog.Action.ASSIGN, symbol, symbol + " <- opal[" + table + "]");
    operation.resolve("datashield.table", table, () -> null);
    operation.evaluate(symbol + " <- opal[" + table + "]", () -> null);
  }

  private void aggregate(String script) {
    DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, script).evaluate(script, () -> null);
  }

  private void aggregate(String submitted, String generated) throws Exception {
    DataShieldTracer.Operation operation = DataShieldTracer.begin(context(), DataShieldLog.Action.AGGREGATE, null, submitted);
    operation.parse(submitted, () -> generated, Function.identity());
    operation.evaluate(generated, () -> null);
  }

  private void close() {
    DataShieldTracer.traced(RID, "default", DataShieldLog.Action.CLOSE, () -> null);
    DataShieldSessionTraces.end(RID);
  }

  private DataShieldContext context() {
    return new DataShieldContext(null, RID, "default", "v2", Map.of());
  }

  private List<SpanContext> attachAuditAppender() {
    List<SpanContext> appended = new ArrayList<>();
    auditAppender = new AppenderBase<>() {
      @Override
      protected void append(ILoggingEvent event) {
        appended.add(Span.current().getSpanContext());
      }
    };
    ch.qos.logback.classic.Logger logger =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("datashield.user");
    auditAppender.setContext(logger.getLoggerContext());
    auditAppender.start();
    logger.addAppender(auditAppender);
    return appended;
  }

  private void detachAuditAppender() {
    ThreadContext.unbindSubject();
    ThreadContext.unbindSecurityManager();
    SecurityUtils.setSecurityManager(null);
    if(auditAppender == null) return;
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("datashield.user")).detachAppender(auditAppender);
    auditAppender.stop();
    auditAppender = null;
  }

  private void login() {
    DefaultSecurityManager securityManager = new DefaultSecurityManager();
    SecurityUtils.setSecurityManager(securityManager);
    ThreadContext.bind(new Subject.Builder(securityManager)
        .principals(new SimplePrincipalCollection("dsuser", "test"))
        .authenticated(true)
        .buildSubject());
  }

  /**
   * The spans by name, the first of a name winning: an operation's evaluation is always named the
   * same, and the tests that need to tell two apart walk the exported list in order instead.
   */
  private Map<String, SpanData> byName() {
    return exporter.getFinishedSpanItems().stream()
        .collect(java.util.stream.Collectors.toMap(SpanData::getName, span -> span, (first, second) -> first));
  }
}
