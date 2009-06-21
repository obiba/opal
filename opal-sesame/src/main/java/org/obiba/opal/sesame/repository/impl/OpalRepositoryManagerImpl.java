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
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * 
 */
public class OpalRepositoryManagerImpl implements OpalRepositoryManager {

  private Map<String, Repository> repositories = new HashMap<String, Repository>();

  private Map<String, String> namespaces = new HashMap<String, String>();

  public void setRepositories(Map<String, Repository> repositories) {
    this.repositories = repositories;
  }

  public void setNamespaces(Map<String, String> namespaces) {
    this.namespaces = namespaces;
  }

  public void initialize() throws RepositoryException {
    for(Entry<String, Repository> entry : repositories.entrySet()) {
      Repository repository = entry.getValue();
      repository.initialize();
      addNamespaces(repository);
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

  protected void addNamespaces(Repository repository) throws RepositoryException {
    RepositoryConnection connection = null;
    try {
      connection = repository.getConnection();
      for(Entry<String, String> namespace : namespaces.entrySet()) {
        if(connection.getNamespace(namespace.getKey()) == null) {
          connection.setNamespace(namespace.getKey(), namespace.getValue());
        }
      }
    } finally {
      if(connection != null) {
        connection.close();
      }
    }
  }

}
