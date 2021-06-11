/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield;

import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.impl.ScriptDSMethod;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.ROperations;

@Deprecated
public class CustomRScriptMethod extends ScriptDSMethod implements DataShieldMethod {

  public CustomRScriptMethod() {

  }

  public CustomRScriptMethod(String name, String script) {
    super(name, script);
  }

  @Override
  public ROperation assign(DSMethodType env) {
    return ROperations.assign(getName(), getScript(), env.symbol(), true);
  }

}
