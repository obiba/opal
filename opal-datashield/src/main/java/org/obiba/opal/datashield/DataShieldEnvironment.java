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

import com.google.common.collect.ImmutableList;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.impl.DefaultDSEnvironment;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.ROperations;

import java.util.List;
import java.util.stream.Collectors;

public class DataShieldEnvironment extends DefaultDSEnvironment {

  @Deprecated
  private DSMethodType environment;

  // XStream ctor
  public DataShieldEnvironment() {
  }

  public DataShieldEnvironment(DSMethodType type) {
    super(type);
  }

  public DataShieldEnvironment(DSMethodType type, List<DefaultDSMethod> methods) {
    super(type, methods);
  }

  public void setEnvironment(DSMethodType environment) {
    setMethodType(environment.name());
  }


  @Override
  public DSMethodType getMethodType() {
    if (environment != null) {
      setMethodType(environment.name());
      environment = null;
    }
    return super.getMethodType();
  }

  /**
   * Returns a sequence of {@code ROperation} instances to run in order to prepare an R environment for executing the
   * methods defined by this {@code DataShieldEnvironment}. Once the operations are executed, an environment is setup
   * and the method {@code DataShieldMethod#invoke(Environment)} will allow obtaining the signature to invoke the
   * method.
   *
   * @return a sequence of {@code ROperation} that will create a protected R environment for executing methods defined.
   */
  public Iterable<ROperation> prepareOps() {
    String envSymbol = getMethodType().symbol();
    List<ROperation> rops = getMethods().stream()
        .filter(m -> !m.hasPackage())
        .map(m -> ROperations.assign(m.getName(), ((DefaultDSMethod) m).getFunction(), envSymbol, true))
        .collect(Collectors.toList());
    if (rops.isEmpty())
      return rops;

    return ImmutableList.<ROperation>builder()//
        .add(ROperations.eval(String.format("base::rm(%s)", envSymbol), null))
        .add(ROperations.assign(getMethodType().symbol(), "base::new.env()"))
        .addAll(rops)
        // Protect the contents of the environment
        .add(ROperations.eval(String.format("base::lockEnvironment(%s, bindings=TRUE)", getMethodType().symbol()), null))//
        // Protect the contents of the environment
        .add(ROperations.eval(String.format("base::lockBinding('%s', base::environment())", getMethodType().symbol()), null))
        .build();
  }

}
