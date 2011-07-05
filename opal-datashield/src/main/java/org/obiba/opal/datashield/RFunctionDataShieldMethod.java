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

import org.obiba.opal.r.AbstractROperationWithResult;
import org.obiba.opal.r.ROperationWithResult;
import org.rosuda.REngine.REXPRaw;

public class RFunctionDataShieldMethod implements DataShieldMethod {

  private String name;

  private String function;

  public RFunctionDataShieldMethod() {

  }

  public RFunctionDataShieldMethod(String name, String function) {
    this.name = name;
    this.function = function;
  }

  public String getName() {
    return name;
  }

  public String getFunction() {
    return function;
  }

  @Override
  public ROperationWithResult asOperation(final REXPRaw argument) {
    return new AbstractROperationWithResult() {

      @Override
      public void doWithConnection() {
        super.assign("agg", argument);
        super.eval(getFunction() + "(unserialize(agg))");
      }
    };
  }
}
