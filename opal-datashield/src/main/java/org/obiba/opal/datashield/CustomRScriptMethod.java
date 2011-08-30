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

import java.util.Map;

import org.obiba.opal.r.AbstractROperationWithResult;
import org.obiba.opal.r.ROperationWithResult;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPRaw;
import org.rosuda.REngine.RList;

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
  public ROperationWithResult asOperation(final REXPRaw result, final Map<String, REXP> arguments) {
    return new AbstractROperationWithResult() {

      @Override
      public void doWithConnection() {
        super.assign("script", getScript());
        super.eval("custom<-base::eval(base::parse(text=script))");
        super.assign("result", result);
        super.assign("arguments", asList(arguments));
        super.eval("base::do.call('custom', c(list(base::unserialize(result)), arguments))");
      }

      private REXPList asList(Map<String, REXP> map) {
        RList list = new RList();
        list.putAll(arguments);
        return new REXPList(list);
      }
    };
  }
}
