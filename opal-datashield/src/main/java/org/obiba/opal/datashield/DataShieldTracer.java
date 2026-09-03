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
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;

/**
 * Spans around the DataSHIELD operations, carrying the same attribute names the audit logs are
 * exported under - see the otelds appender in conf/logback.otel.xml.
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
   * Runs {@code operation} inside a span named after the action, parented to the context captured
   * when {@code context} was built - the request thread, which is not the thread an asynchronous R
   * command runs on.
   */
  public static <T> T traced(DataShieldContext context, DataShieldLog.Action action, String symbol, String script,
      Operation<T> operation) {
    Span span = GlobalOpenTelemetry.getTracer(SCOPE)
        .spanBuilder("datashield." + action.name().toLowerCase())
        .setParent(context.getTraceContext())
        .startSpan();
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
   * For the operations that run on the request thread - opening and closing a session, saving and
   * restoring a workspace - where the current context is already the right parent. The session id is
   * not always known up front; {@link #describeCurrentSession(String)} fills it in afterwards.
   */
  public static <T> T traced(String profile, DataShieldLog.Action action, Operation<T> operation) {
    Span span = GlobalOpenTelemetry.getTracer(SCOPE)
        .spanBuilder("datashield." + action.name().toLowerCase())
        .startSpan();
    span.setAttribute("datashield.action", action.name());
    if(!Strings.isNullOrEmpty(profile)) span.setAttribute("datashield.profile", profile);
    return record(span, action, profile, operation);
  }

  /**
   * Ends the span and counts the operation. The duration is measured here rather than read back off
   * the span so that both signals describe exactly the same interval.
   */
  private static <T> T record(Span span, DataShieldLog.Action action, String profile, Operation<T> operation) {
    long startedAt = System.nanoTime();
    boolean failed = false;
    try(Scope ignored = span.makeCurrent()) {
      return operation.call();
    } catch(Throwable e) {
      failed = true;
      span.setStatus(StatusCode.ERROR, Strings.nullToEmpty(e.getMessage()));
      span.recordException(e);
      throw e;
    } finally {
      span.end();
      DataShieldMetrics.recordOperation(action, profile, failed, System.nanoTime() - startedAt);
    }
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
