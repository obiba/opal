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
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.ROperations;

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
    return ImmutableList.<ROperation>builder()//
        .add(ROperations.eval(String.format("base::rm(%s)", getMethodType().symbol()), null))
        .add(ROperations.assign(getMethodType().symbol(), "base::new.env()"))
        .addAll(getMethods().stream().map(input -> ((DataShieldMethod) input).assign(getMethodType())).collect(Collectors.toList()))//
            // Protect the contents of the environment
        .add(ROperations.eval(String.format("base::lockEnvironment(%s, bindings=TRUE)", getMethodType().symbol()), null))//
            // Protect the contents of the environment
        .add(
            ROperations.eval(String.format("base::lockBinding('%s', base::environment())", getMethodType().symbol()), null))
        .build();
  }

}
