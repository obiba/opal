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
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.obiba.datashield.r.expr.ParseException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Spans around the DataSHIELD operations, carrying the same attribute names the audit logs are
 * exported under - see the otelds appender in conf/logback.xml.
 * <p/>
 * The logs say what was submitted and whether it was refused; the spans say how long the R server
 * took, which is the question the log file cannot answer.
 * <p/>
 * An assignment or an aggregation is one {@link Operation}: a span begun on the request thread that
 * outlives the request, with the check of the submitted expression and its evaluation on the R server
 * as its children. Opening and closing a session, and the workspace operations, are single spans.
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

  public interface Body<T> {
    T call();
  }

  /**
   * Parsing is the one traced step that reports a checked failure, and the one whose failure matters
   * most: a script refused by the parser is a restriction doing its job.
   */
  public interface Parsing<T> {
    T call() throws ParseException;
  }

  /**
   * Begins an assignment or an aggregation, parented to the trace of the session {@code context}
   * belongs to - looked up by session id when the context was built, so it holds on the R session's
   * consumer thread as well as on the request thread.
   * <p/>
   * The operation is not ended here: it ends with its evaluation, or with a refusal, whichever comes
   * first. Until then it is registered against its session, so that a command that is queued but
   * never run is still ended when the session is - see {@link DataShieldSessionTraces#end(String)}.
   */
  public static Operation begin(DataShieldContext context, DataShieldLog.Action action, String symbol, String script) {
    Context parent = context == null ? Context.root() : context.getTraceContext();
    SpanBuilder builder = spanBuilder(action, parent);
    linkToCallingRequest(builder, parent);
    Span span = builder.startSpan();
    describe(span, context, action, symbol, script);
    Operation operation = new Operation(context, action, symbol, span, parent);
    DataShieldSessionTraces.register(context == null ? null : context.getRId(), operation);
    return operation;
  }

  /**
   * For the operations that run on the request thread - opening and closing a session, saving and
   * restoring a workspace. They are parented by session id like every other operation, except when
   * {@code rid} is unknown because the session is what is being created: the caller has then already
   * made the session span current, and {@link #describeCurrentSession(String)} names it afterwards.
   */
  public static <T> T traced(String rid, String profile, DataShieldLog.Action action, Body<T> body) {
    SpanBuilder builder;
    if(Strings.isNullOrEmpty(rid)) {
      // the session is what is being created: the caller has made the session span current
      builder = GlobalOpenTelemetry.getTracer(SCOPE).spanBuilder(spanName(action));
    } else {
      Context parent = DataShieldSessionTraces.contextOf(rid);
      builder = spanBuilder(action, parent);
      linkToCallingRequest(builder, parent);
    }
    Span span = builder.startSpan();
    span.setAttribute("datashield.action", action.name());
    if(!Strings.isNullOrEmpty(rid)) span.setAttribute("datashield.session.id", rid);
    if(!Strings.isNullOrEmpty(profile)) span.setAttribute("datashield.profile", profile);
    return record(span, action, profile, body);
  }

  /**
   * Adds the session id to the span in progress, for the operations that only learn it once the R
   * session exists. A no-op when nothing is being traced.
   */
  public static void describeCurrentSession(String rid) {
    if(!Strings.isNullOrEmpty(rid)) Span.current().setAttribute("datashield.session.id", rid);
  }

  /**
   * One assignment or aggregation, from the request that submitted it to the end of its evaluation.
   * <p/>
   * The check runs on the request thread and ends at once; the evaluation runs on the session's
   * consumer thread, possibly much later. What lies between them is the time the command spent in the
   * session's queue, which is what the operation span shows that its children do not.
   */
  public static final class Operation {

    private final DataShieldContext context;

    private final DataShieldLog.Action action;

    private final String symbol;

    private final Span span;

    private final Context spanContext;

    private final AtomicBoolean ended = new AtomicBoolean();

    private Operation(DataShieldContext context, DataShieldLog.Action action, String symbol, Span span, Context parent) {
      this.context = context;
      this.action = action;
      this.symbol = symbol;
      this.span = span;
      this.spanContext = parent.with(span);
    }

    /**
     * Makes the operation current on the calling thread, so that the audit records written meanwhile
     * are anchored on it rather than on the session.
     */
    public Scope makeCurrent() {
      return spanContext.makeCurrent();
    }

    /**
     * The parsing of the submitted expression, as a child that ends when the parser returns. Records
     * the generated script alongside the submitted one, under the name the PARSE audit record is
     * exported with. A refusal ends the operation as well: nothing will be evaluated.
     */
    public <T> T parse(String submitted, Parsing<T> parsing, Function<? super T, String> generatedScript)
        throws ParseException {
      Span check = child("datashield.parse", DataShieldLog.Action.PARSE);
      if(!Strings.isNullOrEmpty(submitted)) check.setAttribute("datashield.script", submitted);
      long startedAt = System.nanoTime();
      Throwable failure = null;
      try(Scope ignored = check.makeCurrent()) {
        T generator = parsing.call();
        String generated = generator == null ? null : generatedScript.apply(generator);
        if(!Strings.isNullOrEmpty(generated)) check.setAttribute("datashield.script.generated", generated);
        return generator;
      } catch(Throwable e) {
        failure = e;
        throw e;
      } finally {
        finish(check, DataShieldLog.Action.PARSE, profile(), startedAt, failure);
        if(failure != null) end(failure);
      }
    }

    /**
     * The lookup of what a table or a resource assignment refers to, as a child that ends when the
     * lookup returns: the thing exists and the user may read it, or the operation is refused here
     * and never reaches the session's queue.
     */
    public <T> T resolve(String attribute, String value, Body<T> lookup) {
      Span check = child("datashield.resolve", DataShieldLog.Action.RESOLVE);
      if(!Strings.isNullOrEmpty(value)) check.setAttribute(attribute, value);
      long startedAt = System.nanoTime();
      Throwable failure = null;
      try(Scope ignored = check.makeCurrent()) {
        return lookup.call();
      } catch(Throwable e) {
        failure = e;
        throw e;
      } finally {
        finish(check, DataShieldLog.Action.RESOLVE, profile(), startedAt, failure);
        if(failure != null) end(failure);
      }
    }

    /**
     * The evaluation on the R server, as a child that carries the script actually sent to R; the
     * operation ends with it, whichever way it ends. The metric of the operation's action is timed
     * around this interval and no other: it measures the R server, not the queue.
     */
    public <T> T evaluate(String script, Body<T> evaluation) {
      Span eval = child("datashield.eval", action);
      if(!Strings.isNullOrEmpty(script)) eval.setAttribute("datashield.script", script);
      if(!Strings.isNullOrEmpty(symbol)) eval.setAttribute("datashield.symbol", symbol);
      long startedAt = System.nanoTime();
      Throwable failure = null;
      try(Scope ignored = eval.makeCurrent()) {
        return evaluation.call();
      } catch(Throwable e) {
        failure = e;
        throw e;
      } finally {
        finish(eval, action, profile(), startedAt, failure);
        end(failure);
      }
    }

    public void evaluate(String script, Runnable evaluation) {
      evaluate(script, () -> {
        evaluation.run();
        return null;
      });
    }

    /**
     * Ends the operation as refused, for a failure that happened outside the check itself - the
     * checks already do this for their own failures.
     */
    public void refuse(Throwable failure) {
      end(failure);
    }

    /**
     * For {@link DataShieldSessionTraces}: the session is gone and the command was never run.
     */
    void abandon(String reason) {
      if(!ended.compareAndSet(false, true)) return;
      span.setStatus(StatusCode.ERROR, reason);
      span.end();
    }

    boolean isEnded() {
      return ended.get();
    }

    private Span child(String name, DataShieldLog.Action step) {
      // in the operation's trace by construction: no link to the calling request, the parent has it
      Span child = GlobalOpenTelemetry.getTracer(SCOPE).spanBuilder(name).setParent(spanContext).startSpan();
      child.setAttribute("datashield.action", step.name());
      if(context != null) {
        if(!Strings.isNullOrEmpty(context.getRId())) child.setAttribute("datashield.session.id", context.getRId());
        if(!Strings.isNullOrEmpty(context.getProfile())) child.setAttribute("datashield.profile", context.getProfile());
      }
      return child;
    }

    private void end(Throwable failure) {
      if(!ended.compareAndSet(false, true)) return;
      if(failure != null) {
        span.setStatus(StatusCode.ERROR, Strings.nullToEmpty(failure.getMessage()));
        span.recordException(failure);
      }
      span.end();
      DataShieldSessionTraces.unregister(context == null ? null : context.getRId(), this);
    }

    private String profile() {
      return context == null ? null : context.getProfile();
    }
  }

  private static String spanName(DataShieldLog.Action action) {
    return "datashield." + action.name().toLowerCase();
  }

  private static SpanBuilder spanBuilder(DataShieldLog.Action action, Context parent) {
    return GlobalOpenTelemetry.getTracer(SCOPE).spanBuilder(spanName(action)).setParent(parent);
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
  private static <T> T record(Span span, DataShieldLog.Action action, String profile, Body<T> body) {
    long startedAt = System.nanoTime();
    Throwable failure = null;
    try(Scope ignored = span.makeCurrent()) {
      return body.call();
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
