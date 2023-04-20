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

import org.obiba.datashield.r.expr.ParseException;
import org.obiba.opal.spi.r.RSerialize;
import org.slf4j.MDC;

/**
 * Parses a restricted R script, executes it and stores the result.
 */
public class RestrictedRScriptROperation extends AbstractRestrictedRScriptROperation {

  private final RSerialize serialize;

  public RestrictedRScriptROperation(String script, DataShieldContext context, RSerialize serialize) throws ParseException {
    super(script, context);
    this.serialize = serialize;
  }

  @Override
  protected void doWithConnection() {
    super.doWithConnection();
    setResult(null);
    String script = restrictedScript();
    beforeLog(script);
    DataShieldLog.userDebugLog(getContext(), DataShieldLog.Action.AGGREGATE, "evaluating '{}'", script);
    try {
      setResult(eval(script, serialize));
      beforeLog(script);
      DataShieldLog.userLog(getContext(), DataShieldLog.Action.AGGREGATE, "evaluated '{}'", script);
    } catch (Throwable e) {
      beforeLog(script);
      DataShieldLog.userErrorLog(getContext(), DataShieldLog.Action.AGGREGATE, "evaluation failure '{}'", script);
      throw e;
    }
  }

  private void beforeLog(String script) {
    MDC.put("ds_eval", script);
    MDC.put("ds_profile", getContext().getProfile());
    getContext().getContextMap().forEach(MDC::put);
  }
}
