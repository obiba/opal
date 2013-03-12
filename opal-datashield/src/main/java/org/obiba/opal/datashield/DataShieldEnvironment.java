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

import java.util.List;

import org.obiba.opal.datashield.cfg.DatashieldConfiguration.Environment;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperations;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DataShieldEnvironment {

  private Environment environment;

  private List<DataShieldMethod> methods;

  // XStream ctor
  public DataShieldEnvironment() {

  }

  public DataShieldEnvironment(Environment environment, List<DataShieldMethod> methods) {
    Preconditions.checkArgument(environment != null);
    Preconditions.checkArgument(methods != null);
    this.environment = environment;
    this.methods = Lists.newArrayList(methods);
  }

  public Environment getEnvironment() {
    return environment;
  }

  /**
   * Get the registered methods.
   *
   * @return
   */
  public List<DataShieldMethod> getMethods() {
    return ImmutableList.copyOf(methods);
  }

  /**
   * Add or replace the provide method.
   *
   * @param method
   */
  public void addMethod(DataShieldMethod method) {
    for(DataShieldMethod m : getMethods()) {
      if(m.getName().equals(method.getName())) {
        methods.remove(m);
        break;
      }
    }
    methods.add(method);
  }

  /**
   * Remove the method with the given name.
   *
   * @param name
   * @throws NoSuchDataShieldMethodException
   */
  public void removeMethod(String name) {
    methods.remove(getMethod(name));
  }

  /**
   * Check if there is a method with the given name.
   *
   * @param name
   * @return
   */
  public boolean hasMethod(String name) {
    for(DataShieldMethod method : methods) {
      if(method.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the method with from its name.
   *
   * @param name
   * @return
   * @throws NoSuchDataShieldMethodException
   */
  public DataShieldMethod getMethod(String name) {
    for(DataShieldMethod method : methods) {
      if(method.getName().equals(name)) {
        return method;
      }
    }
    throw new NoSuchDataShieldMethodException(name);
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
        .add(ROperations.eval(String.format("base::rm(%s)", environment.symbol()), null))//
        .add(ROperations.assign(environment.symbol(), "base::new.env()"))//
        .addAll(Iterables.transform(getMethods(), new Function<DataShieldMethod, ROperation>() {

          @Override
          public ROperation apply(DataShieldMethod input) {
            return input.assign(environment);
          }
        }))//
            // Protect the contents of the environment
        .add(ROperations.eval(String.format("base::lockEnvironment(%s, bindings=TRUE)", environment.symbol()), null))//
            // Protect the contents of the environment
        .add(
            ROperations.eval(String.format("base::lockBinding('%s', base::environment())", environment.symbol()), null))
        .build();
  }
}
