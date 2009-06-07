/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.repository;

import java.io.File;

import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} that builds {@link SailRepository} from a {@link Sail} instance.
 */
public class SailRepositoryFactoryBean implements FactoryBean {

  private File dataDir;

  private Sail sail;

  /** The repository instance */
  private SailRepository repository;

  public void setDataDir(File dataDir) {
    this.dataDir = dataDir;
  }

  public void setSail(Sail sail) {
    this.sail = sail;
  }

  public Object getObject() throws Exception {
    if(sail == null) throw new IllegalStateException("sail property must be set");
    if(repository == null) {
      newRepository();
    }
    return repository;
  }

  public Class<?> getObjectType() {
    return SailRepository.class;
  }

  public boolean isSingleton() {
    // We always return the same instance
    return true;
  }

  protected synchronized void newRepository() {
    if(repository == null) {
      repository = new SailRepository(sail);
      if(dataDir != null) {
        repository.setDataDir(dataDir);
      }
    }
  }

}
