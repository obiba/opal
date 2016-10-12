/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield;

import javax.annotation.Nullable;

import org.obiba.opal.datashield.cfg.DatashieldConfiguration.Environment;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperations;

public class RFunctionDataShieldMethod implements DataShieldMethod {

  private String name;

  private String function;

  /**
   * R package that defined this method.
   */
  private String rPackage;

  /**
   * Version of the R package.
   */
  private String version;

  public RFunctionDataShieldMethod() {
  }

  public RFunctionDataShieldMethod(String name, String function) {
    this(name, function, null, null);
  }

  public RFunctionDataShieldMethod(String name, String function, String rPackage, String version) {
    this.name = name;
    this.function = function;
    this.rPackage = rPackage;
    this.version = version;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getFunction() {
    return function;
  }

  public boolean hasRPackage() {
    return rPackage != null;
  }

  @Nullable
  public String getRPackage() {
    return rPackage;
  }

  @Nullable
  public String getVersion() {
    return version;
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
