/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.sesame.repository;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

/**
 * 
 */
public interface OpalRepositoryManager {

  public enum OpalRepository {
    CATALOG, DATA
  };

  /**
   * Register a repository.
   * @param name
   * @param repository
   */
  public void register(String name, Repository repository);

  /**
   * Unregister a repository.
   * @param name
   */
  public void unregister(String name);

  /**
   * get a repository;
   * @param name
   * @return
   */
  public Repository getRepository(String name);

  /**
   * Get the catalog repository.
   * @return
   */
  public Repository getCatalogRepository();

  /**
   * Get the data repository.
   * @return
   */
  public Repository getDataRepository();

  /**
   * Initializes the repositories.
   * @throws RepositoryException
   */
  public void initialize() throws RepositoryException;

  /**
   * Shutdown the repositories.
   * @throws RepositoryException
   */
  public void shutdown() throws RepositoryException;

}
