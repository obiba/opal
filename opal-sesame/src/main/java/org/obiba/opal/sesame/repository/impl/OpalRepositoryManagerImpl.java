/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.repository.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.obiba.opal.sesame.repository.OpalRepositoryManager;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

/**
 * 
 */
public class OpalRepositoryManagerImpl implements OpalRepositoryManager {

  private Map<String, Repository> repositories = new HashMap<String, Repository>();

  public void setRepositories(Map<String, Repository> repositories) {
    this.repositories = repositories;
  }

  public void initialize() throws RepositoryException {
    for(Entry<String, Repository> entry : repositories.entrySet()) {
      entry.getValue().initialize();
    }
  }

  public void shutdown() throws RepositoryException {
    for(Entry<String, Repository> entry : repositories.entrySet()) {
      entry.getValue().shutDown();
    }
  }

  public Repository getRepository(String name) {
    return repositories.get(name);
  }

  public void register(String name, Repository repository) {
    repositories.put(name, repository);
  }

  public void unregister(String name) {
    repositories.remove(name);
  }

  public Repository getCatalogRepository() {
    return getRepository(OpalRepository.CATALOG.toString().toLowerCase());
  }

  public Repository getDataRepository() {
    return getRepository(OpalRepository.DATA.toString().toLowerCase());
  }

}
