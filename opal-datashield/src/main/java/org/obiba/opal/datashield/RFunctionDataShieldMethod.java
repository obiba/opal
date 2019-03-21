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

import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.impl.PackagedFunctionDSMethod;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.ROperations;

public class RFunctionDataShieldMethod extends PackagedFunctionDSMethod implements DataShieldMethod {

  @Deprecated
  private String rPackage;

  public RFunctionDataShieldMethod() {
  }

  public RFunctionDataShieldMethod(String name, String function) {
    this(name, function, null, null);
  }

  public RFunctionDataShieldMethod(String name, String function, String rPackage, String version) {
    super(name, function, rPackage, version);
  }
  
  @Override
  public String getPackage() {
    if (rPackage != null) {
      setPackage(rPackage);
      rPackage = null;
    }
    return super.getPackage();
  }

  @Override
  public ROperation assign(DSMethodType env) {
    return ROperations.noOp();
  }

}
