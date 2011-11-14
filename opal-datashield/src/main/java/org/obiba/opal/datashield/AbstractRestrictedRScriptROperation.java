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
import org.obiba.opal.datashield.expr.DataShieldScriptValidator;
import org.obiba.opal.datashield.expr.InvalidScriptException;
import org.obiba.opal.datashield.expr.ParseException;
import org.obiba.opal.datashield.expr.RScriptGenerator;
import org.obiba.opal.datashield.expr.SimpleNode;
import org.obiba.opal.r.AbstractROperationWithResult;

import com.google.common.base.Preconditions;

public abstract class AbstractRestrictedRScriptROperation extends AbstractROperationWithResult {

  private final SimpleNode scriptAst;

  private final DataShieldEnvironment environment;

  public AbstractRestrictedRScriptROperation(String script, DataShieldEnvironment environment, DataShieldScriptValidator validator) throws ParseException, InvalidScriptException {
    super();
    Preconditions.checkArgument(script != null, "script cannot be null");
    Preconditions.checkArgument(environment != null, "environment cannot be null");
    Preconditions.checkArgument(validator != null, "validator cannot be null");

    this.scriptAst = new DataShieldGrammar(new StringReader(script)).root();
    validator.validate(this.scriptAst);
    this.environment = environment;
  }

  protected String restricted() {
    String restrictedScript = new RScriptGenerator(environment.getMethods()).toScript(scriptAst);
    return restrictedScript;
  }

}
