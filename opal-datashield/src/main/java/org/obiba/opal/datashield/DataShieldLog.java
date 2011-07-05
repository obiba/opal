/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DataShieldLog {

  private static final Logger adminLog = LoggerFactory.getLogger("datashield.admin");

  private static final Logger userLog = LoggerFactory.getLogger("datashield.user");

  public static final void adminLog(String format, Object... arguments) {
    log(adminLog, format, arguments);
  }

  public static final void userLog(String format, Object... arguments) {
    log(userLog, format, arguments);
  }

  private static final void log(Logger log, String format, Object... arguments) {
    log.info("User '" + SecurityUtils.getSubject().getPrincipal().toString() + "' " + format, arguments);
  }

}
