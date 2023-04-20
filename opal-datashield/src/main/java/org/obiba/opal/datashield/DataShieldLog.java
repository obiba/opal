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
    LS
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
    userLog.debug(format, arguments);
    init();
  }

  public static void userDebugLog(String id, Action action, String format, Object... arguments) {
    if (!userLog.isDebugEnabled()) return;
    prepare(id, action);
    userLog.debug(format, arguments);
    init();
  }

  public static void userLog(DataShieldContext context, Action action, String format, Object... arguments) {
    prepare(context, action);
    userLog.info(format, arguments);
    init();
  }

  public static void userLog(String id, Action action, String format, Object... arguments) {
    prepare(id, action);
    userLog.info(format, arguments);
    init();
  }

  public static void userErrorLog(DataShieldContext context, Action action, String format, Object... arguments) {
    prepare(context, action);
    userLog.error(format, arguments);
    init();
  }

  public static void userErrorLog(String id, Action action, String format, Object... arguments) {
    prepare(id, action);
    userLog.error(format, arguments);
    init();
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
