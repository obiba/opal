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

import com.google.common.base.Strings;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import org.obiba.datashield.r.expr.ParseException;

/**
 * Spans around the DataSHIELD operations, carrying the same attribute names the audit logs are
 * exported under - see the otelds appender in conf/logback.xml.
 * <p/>
 * The logs say what was submitted and whether it was refused; the spans say how long the R server
 * took, which is the question the log file cannot answer.
 * <p/>
 * No SDK, no cost: OpalServer only builds one when an OTLP endpoint is configured, and until then
 * {@link GlobalOpenTelemetry#getOrNoop()} returns no-op instruments that allocate nothing.
 */
public final class DataShieldTracer {

  /**
   * The instrumentation scope reported to the backend. Deliberately distinct from the logger names,
   * which identify the audit streams.
   */
  private static final String SCOPE = "org.obiba.opal.datashield";

  private DataShieldTracer() {
  }

  public interface Operation<T> {
    T call();
  }

  /**
   * Parsing is the one traced operation that reports a checked failure, and the one whose failure
   * matters most: a script refused by the parser is a restriction doing its job.
   */
  public interface ParsingOperation<T> {
    T call() throws ParseException;
  }

  /**
   * Runs {@code operation} inside a span named after the action, parented to the trace of the
   * session {@code context} belongs to - looked up by session id when the context was built, so it
   * holds on the R session's consumer thread as well as on the request thread.
   */
  public static <T> T traced(DataShieldContext context, DataShieldLog.Action action, String symbol, String script,
      Operation<T> operation) {
    Context parent = context == null ? Context.root() : context.getTraceContext();
    Span span = operationSpan(action, parent).startSpan();
    describe(span, context, action, symbol, script);
    return record(span, action, context == null ? null : context.getProfile(), operation);
  }

  public static void traced(DataShieldContext context, DataShieldLog.Action action, String symbol, String script,
      Runnable operation) {
    traced(context, action, symbol, script, () -> {
      operation.run();
      return null;
    });
  }

  /**
   * Traces the parsing of a submitted expression, which happens on the request thread before the R
   * command it produces is queued - so it is a span of its own, alongside the evaluation rather than
   * around it, exactly as the audit log records it.
   */
  public static <T> T tracedParse(DataShieldContext context, String script, ParsingOperation<T> operation)
      throws ParseException {
    Span span = operationSpan(DataShieldLog.Action.PARSE, context.getTraceContext()).startSpan();
    describe(span, context, DataShieldLog.Action.PARSE, null, script);
    long startedAt = System.nanoTime();
    Throwable failure = null;
    try(Scope ignored = span.makeCurrent()) {
      return operation.call();
    } catch(Throwable e) {
      failure = e;
      throw e;
    } finally {
      finish(span, DataShieldLog.Action.PARSE, context.getProfile(), startedAt, failure);
    }
  }

  /**
   * For the operations that run on the request thread - opening and closing a session, saving and
   * restoring a workspace. They are parented by session id like every other operation, except when
   * {@code rid} is unknown because the session is what is being created: the caller has then already
   * made the session span current, and {@link #describeCurrentSession(String)} names it afterwards.
   */
  public static <T> T traced(String rid, String profile, DataShieldLog.Action action, Operation<T> operation) {
    SpanBuilder builder = Strings.isNullOrEmpty(rid)
        // the session is what is being created: the caller has made the session span current
        ? GlobalOpenTelemetry.getTracer(SCOPE).spanBuilder("datashield." + action.name().toLowerCase())
        : operationSpan(action, DataShieldSessionTraces.contextOf(rid));
    Span span = builder.startSpan();
    span.setAttribute("datashield.action", action.name());
    if(!Strings.isNullOrEmpty(rid)) span.setAttribute("datashield.session.id", rid);
    if(!Strings.isNullOrEmpty(profile)) span.setAttribute("datashield.profile", profile);
    return record(span, action, profile, operation);
  }

  private static SpanBuilder operationSpan(DataShieldLog.Action action, Context parent) {
    SpanBuilder builder = GlobalOpenTelemetry.getTracer(SCOPE)
        .spanBuilder("datashield." + action.name().toLowerCase())
        .setParent(parent);
    linkToCallingRequest(builder, parent);
    return builder;
  }

  /**
   * Links the span to the request that asked for the operation, when that request is in a trace of
   * its own.
   * <p/>
   * It is whenever something else instruments the HTTP layer - the OpenTelemetry Java agent, say -
   * because a DataSHIELD trace is deliberately rooted on the session rather than on a request, so
   * the two traces would otherwise have no way to find each other. Nothing instruments the HTTP
   * layer by default, and then there is no request span and no link.
   */
  static void linkToCallingRequest(SpanBuilder builder, Context parent) {
    SpanContext caller = Span.current().getSpanContext();
    if(!caller.isValid()) return;
    SpanContext parentSpan = Span.fromContext(parent).getSpanContext();
    // already in the same trace: the parent relationship says it, a link would only repeat it
    if(parentSpan.isValid() && caller.getTraceId().equals(parentSpan.getTraceId())) return;
    builder.addLink(caller);
  }

  /**
   * Ends the span and counts the operation. The duration is measured here rather than read back off
   * the span so that both signals describe exactly the same interval.
   */
  private static <T> T record(Span span, DataShieldLog.Action action, String profile, Operation<T> operation) {
    long startedAt = System.nanoTime();
    Throwable failure = null;
    try(Scope ignored = span.makeCurrent()) {
      return operation.call();
    } catch(Throwable e) {
      failure = e;
      throw e;
    } finally {
      finish(span, action, profile, startedAt, failure);
    }
  }

  private static void finish(Span span, DataShieldLog.Action action, String profile, long startedAt,
      Throwable failure) {
    if(failure != null) {
      span.setStatus(StatusCode.ERROR, Strings.nullToEmpty(failure.getMessage()));
      span.recordException(failure);
    }
    span.end();
    DataShieldMetrics.recordOperation(action, profile, failure != null, System.nanoTime() - startedAt);
  }

  /**
   * Adds the session id to the span in progress, for the operations that only learn it once the R
   * session exists. A no-op when nothing is being traced.
   */
  public static void describeCurrentSession(String rid) {
    if(!Strings.isNullOrEmpty(rid)) Span.current().setAttribute("datashield.session.id", rid);
  }

  private static void describe(Span span, DataShieldContext context, DataShieldLog.Action action, String symbol,
      String script) {
    span.setAttribute("datashield.action", action.name());
    if(context != null) {
      if(!Strings.isNullOrEmpty(context.getRId())) span.setAttribute("datashield.session.id", context.getRId());
      if(!Strings.isNullOrEmpty(context.getProfile())) span.setAttribute("datashield.profile", context.getProfile());
    }
    if(!Strings.isNullOrEmpty(symbol)) span.setAttribute("datashield.symbol", symbol);
    // the submitted expression, as on the audit log: it is what a disclosure attempt looks like
    if(!Strings.isNullOrEmpty(script)) span.setAttribute("datashield.script", script);
  }
}
