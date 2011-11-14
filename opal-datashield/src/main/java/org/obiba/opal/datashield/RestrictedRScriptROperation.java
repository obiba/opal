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

import org.obiba.opal.datashield.expr.DataShieldScriptValidator;
import org.obiba.opal.datashield.expr.ParseException;

/**
 * Parses a restricted R script, executes it and stores the result.
 */
public class RestrictedRScriptROperation extends AbstractRestrictedRScriptROperation {

  public RestrictedRScriptROperation(String script, DataShieldEnvironment environment, DataShieldScriptValidator validator) throws ParseException {
    super(script, environment, validator);
  }

  public RestrictedRScriptROperation(String script, DataShieldEnvironment environment) throws ParseException {
    this(script, environment, new DataShieldScriptValidator());
  }

  @Override
  protected void doWithConnection() {
    setResult(null);
    String script = super.restricted();
    String eval = String.format("eval({%s})", script);
    DataShieldLog.userLog("evaluating {}", script);
    setResult(eval(eval));
  }
}
