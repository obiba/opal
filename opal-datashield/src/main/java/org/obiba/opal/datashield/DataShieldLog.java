/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield;

import com.google.common.base.Strings;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 */
public class DataShieldLog {

  public enum Action {
    OPEN,
    AGGREGATE,
    ASSIGN,
    CLOSE,
    PARSE,
    WS_SAVE,
    WS_RESTORE,
    RM,
    LS,
    QUOTA
  }

  private static final Logger adminLog = LoggerFactory.getLogger("datashield.admin");

  private static final Logger userLog = LoggerFactory.getLogger("datashield.user");

  private DataShieldLog() {
  }

  public static void adminLog(String format, Object... arguments) {
    adminLog.info(format, arguments);
  }

  public static void init() {
    String ip = MDC.get("ip");
    MDC.clear();
    MDC.put("ip", ip);
  }

  public static void userDebugLog(DataShieldContext context, Action action, String format, Object... arguments) {
    if (!userLog.isDebugEnabled()) return;
    prepare(context, action);
    inSessionTrace(context.getRId(), () -> userLog.debug(format, arguments));
    init();
  }

  public static void userDebugLog(String id, Action action, String format, Object... arguments) {
    if (!userLog.isDebugEnabled()) return;
    prepare(id, action);
    inSessionTrace(id, () -> userLog.debug(format, arguments));
    init();
  }

  public static void userLog(DataShieldContext context, Action action, String format, Object... arguments) {
    prepare(context, action);
    inSessionTrace(context.getRId(), () -> userLog.info(format, arguments));
    init();
  }

  public static void userLog(String id, Action action, String format, Object... arguments) {
    prepare(id, action);
    inSessionTrace(id, () -> userLog.info(format, arguments));
    init();
  }

  public static void userErrorLog(DataShieldContext context, Action action, String format, Object... arguments) {
    prepare(context, action);
    inSessionTrace(context.getRId(), () -> userLog.error(format, arguments));
    init();
  }

  public static void userErrorLog(String id, Action action, String format, Object... arguments) {
    prepare(id, action);
    inSessionTrace(id, () -> userLog.error(format, arguments));
    init();
  }

  /**
   * Writes the record while the trace of the session it belongs to is current, so that the exported
   * copy carries that session's trace id: the audit trail of a session and its spans are then two
   * views of one thing rather than two unrelated streams. Most of these records are written just
   * after the span of the operation has been closed, hence the lookup by session id - but a caller
   * that is still inside a span keeps it, as the more precise of the two.
   * <p/>
   * Nothing of this reaches datashield.log, whose format is fixed: the ids belong to the exported
   * log record, not to the MDC the file encoder writes.
   */
  private static void inSessionTrace(String rid, Runnable record) {
    if (Span.current().getSpanContext().isValid()) {
      record.run();
      return;
    }
    try (Scope ignored = DataShieldSessionTraces.contextOf(rid).makeCurrent()) {
      record.run();
    }
  }

  private static void prepare(DataShieldContext context, Action action) {
    prepare(context.getRId(), action);
    MDC.put("ds_profile", context.getProfile());
    context.getContextMap().forEach(MDC::put);
  }

  private static void prepare(String id, Action action) {
    if (!Strings.isNullOrEmpty(id)) MDC.put("ds_id", id);
    MDC.put("username", SecurityUtils.getSubject().getPrincipal().toString());
    MDC.put("ds_action", action == null ? "?" : action.name());
  }

}
