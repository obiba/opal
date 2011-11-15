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

import com.google.common.base.Preconditions;

/**
 * Parses a restricted R script, executes it and assigns the result to a symbol.
 */
public class RestrictedAssignmentROperation extends AbstractRestrictedRScriptROperation {

  private final String symbol;

  public RestrictedAssignmentROperation(String symbol, String script, DataShieldEnvironment environment, DataShieldScriptValidator validator) throws ParseException {
    super(script, environment, validator);
    Preconditions.checkArgument(symbol != null, "symbol cannot be null");
    this.symbol = symbol;
  }

  public RestrictedAssignmentROperation(String symbol, String script, DataShieldEnvironment environment) throws ParseException {
    this(symbol, script, environment, new DataShieldScriptValidator());
  }

  @Override
  protected void doWithConnection() {
    super.doWithConnection();
    setResult(null);
    String script = super.restricted();
    DataShieldLog.userLog("assigning '{}' with {}", symbol, script);
    super.eval(String.format("base::assign('%s', value={%s})", symbol, script));
  }
}
