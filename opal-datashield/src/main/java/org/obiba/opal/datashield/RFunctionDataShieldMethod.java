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

import org.obiba.opal.datashield.cfg.DatashieldConfiguration.Environment;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperations;

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
  public ROperation assign(Environment env) {
    return ROperations.noOp();
  }

  @Override
  public String invoke(Environment env) {
    return getFunction();
  }
}
