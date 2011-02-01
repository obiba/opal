/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield.cfg;

import java.util.List;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.datashield.NoSuchDataShieldMethodException;

public class DatashieldConfiguration implements OpalConfigurationExtension {

  private List<DataShieldMethod> aggregatingMethods;

  /**
   * Get the registered aggregating methods.
   * @return
   */
  public List<DataShieldMethod> getAggregatingMethods() {
    return aggregatingMethods;
  }

  /**
   * Add or replace the provide method.
   * @param method
   */
  public void addAggregatingMethod(DataShieldMethod method) {
    for(DataShieldMethod m : aggregatingMethods) {
      if(m.getName().equals(method.getName())) {
        aggregatingMethods.remove(m);
        break;
      }
    }
    aggregatingMethods.add(method);
  }

  /**
   * Remove the method with the given name.
   * @param name
   * @throws NoSuchDataShieldMethodException
   */
  public void removeAggregatingMethod(String name) {
    for(DataShieldMethod method : aggregatingMethods) {
      if(method.getName().equals(name)) {
        aggregatingMethods.remove(method);
        return;
      }
    }
    throw new NoSuchDataShieldMethodException(name);
  }

  /**
   * Check if there is a method with the given name.
   * @param name
   * @return
   */
  public boolean hasDataShieldMethod(String name) {
    for(DataShieldMethod method : aggregatingMethods) {
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
  public DataShieldMethod getDataShieldMethod(String name) {
    for(DataShieldMethod method : aggregatingMethods) {
      if(method.getName().equals(name)) {
        return method;
      }
    }
    throw new NoSuchDataShieldMethodException(name);
  }
}
