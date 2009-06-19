/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.elmo;

import org.openrdf.elmo.ElmoModule;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.repository.Repository;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 */
public class SesameManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

  private Repository repository;

  private boolean handleRepositoryLifecycle = false;

  private boolean inferencingEnabled = false;

  private SesameManagerFactory factory;

  public void setInferencingEnabled(boolean inferencingEnabled) {
    this.inferencingEnabled = inferencingEnabled;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public void setHandleRepositoryLifecycle(boolean handleRepositoryLifecycle) {
    this.handleRepositoryLifecycle = handleRepositoryLifecycle;
  }

  public boolean isHandleRepositoryLifecycle() {
    return handleRepositoryLifecycle;
  }

  public Object getObject() throws Exception {
    if(factory == null) {
      createSesameManager();
    }
    return factory;
  }

  public Class<?> getObjectType() {
    return SesameManagerFactory.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void afterPropertiesSet() throws Exception {
    if(isHandleRepositoryLifecycle()) {
      repository.initialize();
    }
  }

  public void destroy() throws Exception {
    if(factory != null) {
      factory.close();
    }

    if(isHandleRepositoryLifecycle()) {
      repository.shutDown();
    }
  }

  synchronized protected SesameManagerFactory createSesameManager() {
    if(factory == null) {
      ElmoModule module = new ElmoModule();
      factory = new SesameManagerFactory(module, repository);
      factory.setInferencingEnabled(inferencingEnabled);
    }
    return factory;
  }

}
