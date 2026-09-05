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
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.Scope;
import org.apache.shiro.SecurityUtils;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The span every operation of a DataSHIELD session hangs under, so that one session is one trace.
 * <p/>
 * A trace is normally scoped to a request, and each of the operations of a session is a request of
 * its own: opening the session, assigning a symbol, parsing and evaluating an expression, closing.
 * Parenting them to whatever context the request thread carries therefore produces one trace per
 * operation, which says nothing a single log line does not. What the audit trail is actually read
 * along is the session - the unit the id in the log identifies - so the session is what the trace
 * has to be, and the session id is the only thing that ties its operations together across threads
 * and requests. This registry holds one open span per session id and hands it out as a parent.
 * <p/>
 * The span stays open for the whole session and so is only exported when the session ends, which is
 * why {@link #retain(Set)} exists: a session is not always closed by its user.
 */
public final class DataShieldSessionTraces {

  private static final String SCOPE = "org.obiba.opal.datashield";

  /**
   * Carries the session span being opened down to {@link #bind(String, String)}, which is called
   * from inside {@link #opening(Supplier)} and only then knows the session id.
   */
  private static final ContextKey<Opening> OPENING = ContextKey.named("opal-datashield-session-opening");

  private static final Map<String, Span> SESSIONS = new ConcurrentHashMap<>();

  private DataShieldSessionTraces() {
  }

  /**
   * Runs the creation of a session under a span that outlives it. The span is a root, and the OPEN
   * span the creation is traced with becomes its first child.
   */
  public static <T> T opening(Supplier<T> creation) {
    SpanBuilder builder = GlobalOpenTelemetry.getTracer(SCOPE).spanBuilder("datashield.session")
        .setNoParent();
    // the trace is the session's, not the request's - but the request that opened it is worth being
    // able to reach, so it is linked when something has traced it
    DataShieldTracer.linkToCallingRequest(builder, Context.root());
    Span session = builder.startSpan();
    Opening opening = new Opening(session);
    try(Scope ignored = Context.root().with(OPENING, opening).with(session).makeCurrent()) {
      return creation.get();
    } catch(Throwable e) {
      session.setStatus(StatusCode.ERROR, Strings.nullToEmpty(e.getMessage()));
      session.recordException(e);
      throw e;
    } finally {
      // a refused or failed creation leaves no session behind to end the span later on
      if(!opening.bound) session.end();
    }
  }

  /**
   * Names the session being opened, once the R server has given it an id. Until this is called the
   * span belongs to nobody and is ended when {@link #opening(Supplier)} returns.
   */
  public static void bind(String rid, String profile) {
    Opening opening = Context.current().get(OPENING);
    if(opening == null || Strings.isNullOrEmpty(rid)) return;
    opening.span.setAttribute("datashield.session.id", rid);
    if(!Strings.isNullOrEmpty(profile)) opening.span.setAttribute("datashield.profile", profile);
    // who and from where, under the names the audit records are exported with: a trace list is then
    // searchable on the two questions an auditor starts from
    describe(opening.span, "enduser.id", principal());
    describe(opening.span, "client.address", MDC.get("ip"));
    SESSIONS.put(rid, opening.span);
    opening.bound = true;
  }

  /**
   * The parent of the operations of {@code rid}. {@link Context#root()} for a session that was open
   * before telemetry was switched on, or that was never traced: its operations are then traced one
   * by one, as they were before.
   */
  public static Context contextOf(String rid) {
    Span session = Strings.isNullOrEmpty(rid) ? null : SESSIONS.get(rid);
    return session == null ? Context.root() : Context.root().with(session);
  }

  /**
   * Closes the trace of a session. Called after the CLOSE span has ended, so that it is part of it.
   */
  public static void end(String rid) {
    if(Strings.isNullOrEmpty(rid)) return;
    Span session = SESSIONS.remove(rid);
    if(session != null) session.end();
  }

  /**
   * Ends the traces of the sessions that are gone without having been closed - expired by the R
   * session reaper, dropped with the R server, refused a profile. Their spans would otherwise stay
   * open forever, and an open span is never exported: the whole trace would be lost, not just its
   * root.
   */
  public static void retain(Set<String> liveSessionIds) {
    retain(() -> liveSessionIds);
  }

  /**
   * As {@link #retain(Set)}, but the open traces are listed <em>before</em> the live sessions are
   * asked for. The manager holds a session before its trace is bound, so a trace bound between the
   * two snapshots is not listed and cannot be mistaken for the trace of a session that is gone - the
   * other order would end it, and orphan every operation the session goes on to run.
   */
  public static void retain(Supplier<Set<String>> liveSessionIds) {
    List<String> open = List.copyOf(SESSIONS.keySet());
    Set<String> live = liveSessionIds.get();
    open.stream().filter(rid -> !live.contains(rid)).forEach(DataShieldSessionTraces::end);
  }

  /**
   * On shutdown: end what is open so the traces of the sessions the restart is about to drop are
   * exported before the SDK closes.
   */
  public static void endAll() {
    retain(Set.of());
  }

  private static void describe(Span span, String attribute, String value) {
    if(!Strings.isNullOrEmpty(value)) span.setAttribute(attribute, value);
  }

  /**
   * The authenticated user, or nothing: a session is always opened by one, but telemetry is not a
   * reason for anything to fail, least of all on the way in.
   */
  private static String principal() {
    try {
      Object principal = SecurityUtils.getSubject().getPrincipal();
      return principal == null ? null : principal.toString();
    } catch(RuntimeException e) {
      return null;
    }
  }

  static int openTraceCount() {
    return SESSIONS.size();
  }

  private static final class Opening {

    private final Span span;

    private boolean bound;

    private Opening(Span span) {
      this.span = span;
    }
  }
}
