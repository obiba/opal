/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.sesame.repository.impl;

import java.io.File;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.springframework.beans.factory.FactoryBean;

/**
 * 
 */
public class MemoryRepositoryFactory implements FactoryBean {

  private File dataDir;

  private Boolean withInferencer;

  public void setDataDir(File dataDir) {
    this.dataDir = dataDir;
  }

  public void setWithInferencer(Boolean withInferencer) {
    this.withInferencer = withInferencer;
  }

  public Object getObject() throws Exception {
    MemoryStore store = new MemoryStore();

    if(dataDir != null) {
      store.setDataDir(dataDir);
    }

    if(withInferencer != null && withInferencer) {
      return new SailRepository(new ForwardChainingRDFSInferencer(store));
    } else {
      return new SailRepository(store);
    }
  }

  public Class getObjectType() {
    return Repository.class;
  }

  public boolean isSingleton() {
    return false;
  }

}
