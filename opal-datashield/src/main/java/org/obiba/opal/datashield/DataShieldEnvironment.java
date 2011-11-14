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

import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.service.OpalRSession;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class DataShieldEnvironment {

  private List<DataShieldMethod> methods;

  /**
   * Get the registered methods.
   * @return
   */
  public List<DataShieldMethod> getMethods() {
    return ImmutableList.copyOf(methods);
  }

  /**
   * Add or replace the provide method.
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
   * @param name
   * @throws NoSuchDataShieldMethodException
   */
  public void removeMethod(String name) {
    methods.remove(getMethod(name));
  }

  /**
   * Check if there is a method with the given name.
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
   * @param name
   * @throws NoSuchDataShieldMethodException
   * @return
   */
  public DataShieldMethod getMethod(String name) {
    for(DataShieldMethod method : methods) {
      if(method.getName().equals(name)) {
        return method;
      }
    }
    throw new NoSuchDataShieldMethodException(name);
  }

  public void prepare(OpalRSession session) {
    session.execute(Iterables.transform(getMethods(), new Function<DataShieldMethod, ROperation>() {

      @Override
      public ROperation apply(DataShieldMethod input) {
        return input.assign();
      }
    }));
  }

}
