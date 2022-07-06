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
    RM,
    CLOSE,
    PARSED,
    PARSE_ERROR
  }

  private static final Logger adminLog = LoggerFactory.getLogger("datashield.admin");

  private static final Logger userLog = LoggerFactory.getLogger("datashield.user");

  private DataShieldLog() {
  }

  public static void adminLog(String format, Object... arguments) {
    log(adminLog, format, arguments);
  }

  public static void init() {
    MDC.put("ds_symbol", null);
    MDC.put("ds_table", null);
    MDC.put("ds_resource", null);
    MDC.put("ds_expr", null);
    MDC.put("ds_script_in", null);
    MDC.put("ds_script_out", null);
    MDC.put("ds_func", null);
  }
  public static void userLog(String id, Action action, String format, Object... arguments) {
    if (!Strings.isNullOrEmpty(id)) MDC.put("rid", id);
    MDC.put("username", SecurityUtils.getSubject().getPrincipal().toString());
    MDC.put("ds_action", action == null ? "?" : action.name());
    //MDC.put("profile", profile);
    if (Action.PARSE_ERROR.equals(action))
        logError(userLog, format, arguments);
    else
      log(userLog, format, arguments);
  }

  private static void log(Logger log, String format, Object... arguments) {
    //noinspection StringConcatenationArgumentToLogCall
    log.info(format, arguments);
  }
  private static void logError(Logger log, String format, Object... arguments) {
    //noinspection StringConcatenationArgumentToLogCall
    log.error(format, arguments);
  }

}
