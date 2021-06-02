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
import org.obiba.datashield.core.impl.PackagedFunctionDSMethod;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.ROperations;

@Deprecated
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
  public boolean hasPackage() {
    upgrade();
    return super.hasPackage();
  }

  @Override
  public void setPackage(String pack) {
    upgrade();
    super.setPackage(pack);
  }

  @Override
  public String getPackage() {
    upgrade();
    return super.getPackage();
  }

  @Override
  public void setVersion(String version) {
    upgrade();
    super.setVersion(version);
  }

  @Override
  public String getVersion() {
    upgrade();
    return super.getVersion();
  }

  @Override
  public ROperation assign(DSMethodType env) {
    return ROperations.noOp();
  }

  /**
   * Upgrade DataSHIELD configuration on the fly.
   */
  private void upgrade() {
    if (rPackage != null) {
      super.setPackage(rPackage);
      rPackage = null;
    }
  }
}
