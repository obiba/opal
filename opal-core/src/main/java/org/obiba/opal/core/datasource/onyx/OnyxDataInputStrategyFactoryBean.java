/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.datasource.onyx;

import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class OnyxDataInputStrategyFactoryBean implements FactoryBean, ApplicationContextAware, InitializingBean {
  //
  // Instance Variables
  //

  private ApplicationContext applicationContext;

  private List<String> chainedStrategies;

  //
  // ApplicationContextAware Methods
  //

  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  // InitializingBean Methods
  //

  public void afterPropertiesSet() throws Exception {
    if(chainedStrategies == null) {
      throw new IllegalStateException("chainedStrategies attribute must be set");
    }
    if(chainedStrategies.size() == 0) {
      throw new IllegalStateException("chainedStrategies attribute must contain at least one element");
    }
  }

  //
  // FactoryBean Methods
  //

  public Object getObject() throws Exception {
    // Build the strategy chain.
    IOnyxDataInputStrategy firstStrategy = (IOnyxDataInputStrategy) applicationContext.getBean(chainedStrategies.get(0));

    IOnyxDataInputStrategy previousStrategy = firstStrategy;
    for(int i = 1; i < chainedStrategies.size(); i++) {
      IOnyxDataInputStrategy delegate = (IOnyxDataInputStrategy) applicationContext.getBean(chainedStrategies.get(i));
      if(previousStrategy instanceof IChainingOnyxDataInputStrategy) {
        ((IChainingOnyxDataInputStrategy) previousStrategy).setDelegate(delegate);
        previousStrategy = delegate;
      } else {
        throw new IllegalArgumentException("Strategy type must implement IChainingOnyxDataInputStrategy in order to be chained with another. Violating type is " + previousStrategy.getClass().getName());
      }
    }

    return firstStrategy;
  }

  public Class<?> getObjectType() {
    return IOnyxDataInputStrategy.class;
  }

  public boolean isSingleton() {
    return false;
  }

  //
  // Methods
  //

  public void setChainedStrategies(List<String> chainedStrategies) {
    this.chainedStrategies = chainedStrategies;
  }
}
