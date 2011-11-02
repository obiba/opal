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

import java.io.StringReader;

import org.obiba.opal.datashield.expr.DataShieldGrammar;
import org.obiba.opal.datashield.expr.ParseException;
import org.obiba.opal.datashield.expr.RScriptGenerator;
import org.obiba.opal.datashield.expr.SimpleNode;
import org.obiba.opal.r.AbstractROperationWithResult;

import com.google.common.base.Preconditions;

/**
 * Parses a restricted R script, executes it and stores the result.
 */
public class RestrictedRScriptROperation extends AbstractROperationWithResult {

  private final String symbol;

  private final SimpleNode scriptAst;

  public RestrictedRScriptROperation(String symbol, String script) throws ParseException {
    super();
    Preconditions.checkArgument(symbol != null, "symbol cannot be null");
    Preconditions.checkArgument(script != null, "script cannot be null");
    this.symbol = symbol;
    this.scriptAst = new DataShieldGrammar(new StringReader(script)).root();
  }

  @Override
  protected void doWithConnection() {
    setResult(null);
    setResult(eval(symbol + "<-" + new RScriptGenerator().toScript(scriptAst)));
  }
}
