/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield;

import org.obiba.opal.r.AbstractROperationWithResult;
import org.obiba.opal.r.ROperationWithResult;
import org.rosuda.REngine.REXPRaw;

public class CustomRScriptMethod implements DataShieldMethod {

  private String name;

  private String script;

  public CustomRScriptMethod() {

  }

  public CustomRScriptMethod(String name, String script) {
    this.name = name;
    this.script = script;
  }

  public String getName() {
    return name;
  }

  public String getScript() {
    return script;
  }

  @Override
  public ROperationWithResult asOperation(final REXPRaw argument) {
    return new AbstractROperationWithResult() {

      @Override
      public void doWithConnection() {
        super.assign("script", getScript());
        super.eval("custom<-eval(base::parse(text=script))");
        super.assign("agg", argument);
        super.eval("custom(unserialize(agg))");
      }
    };
  }
}
