/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield;

import com.google.common.base.Preconditions;
import org.obiba.datashield.core.DSEnvironment;
import org.obiba.datashield.r.expr.DSRScriptValidator;
import org.obiba.datashield.r.expr.ParseException;

/**
 * Parses a restricted R script, executes it and assigns the result to a symbol.
 */
public class RestrictedAssignmentROperation extends AbstractRestrictedRScriptROperation {

  private final String symbol;

  public RestrictedAssignmentROperation(String symbol, String script, DSEnvironment environment,
                                        DSRScriptValidator validator) throws ParseException {
    super(script, environment, validator);
    Preconditions.checkArgument(symbol != null, "symbol cannot be null");
    this.symbol = symbol;
  }

  public RestrictedAssignmentROperation(String symbol, String script, DSEnvironment environment)
      throws ParseException {
    this(symbol, script, environment, new DSRScriptValidator());
  }

  @Override
  protected void doWithConnection() {
    super.doWithConnection();
    setResult(null);
    String script = restricted();
    DataShieldLog.userLog("assigning '{}' with {}", symbol, script);
    eval(String.format("is.null(base::assign('%s', value={%s}))", symbol, script));
  }

  @Override
  public boolean isIgnoreResult() {
    return true;
  }
}
