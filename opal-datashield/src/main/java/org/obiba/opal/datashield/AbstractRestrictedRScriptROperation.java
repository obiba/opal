/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import org.obiba.datashield.r.expr.*;
import org.obiba.opal.spi.r.AbstractROperationWithResult;
import org.obiba.opal.spi.r.ROperation;

import java.io.StringReader;

public abstract class AbstractRestrictedRScriptROperation extends AbstractROperationWithResult {

  private final String script;

  private final SimpleNode scriptAst;

  private final DSEnvironment environment;

  @SuppressWarnings("ConstantConditions")
  public AbstractRestrictedRScriptROperation(String script, DSEnvironment environment,
      DSRScriptValidator validator) throws ParseException, InvalidScriptException {
    Preconditions.checkArgument(script != null, "script cannot be null");
    Preconditions.checkArgument(environment != null, "environment cannot be null");
    Preconditions.checkArgument(validator != null, "validator cannot be null");

    this.script = script;
    this.environment = environment;

    DataShieldLog.userLog("parsing '{}'", script);
    scriptAst = new DataShieldGrammar(new StringReader(script)).root();
    try {
      validator.validate(scriptAst);
    } catch(InvalidScriptException e) {
      DataShieldLog.userLog("Script failed validation: " + e.getMessage());
      throw e;
    }
  }

  @Override
  protected void doWithConnection() {
    Iterable<ROperation> ops = ((DataShieldEnvironment)environment).prepareOps();
    for(ROperation op : ops) {
      op.doWithConnection(getConnection());
    }
  }

  protected String restricted() {
    return new RScriptGenerator(environment).toScript(scriptAst);
  }

  @Override
  public String toString() {
    return script;
  }
}
