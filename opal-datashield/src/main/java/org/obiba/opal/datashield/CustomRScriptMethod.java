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

import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperations;

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
  public ROperation assign() {
    return ROperations.assign(getName(), getScript());
  }

  @Override
  public String invoke() {
    return getName();
  }

}
