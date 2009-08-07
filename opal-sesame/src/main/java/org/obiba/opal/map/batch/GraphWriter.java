/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.map.batch;

import java.util.List;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.batch.item.ItemWriter;

/**
 *
 */
public class GraphWriter implements ItemWriter<Graph> {

  private Repository repository;

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public void write(List<? extends Graph> items) throws Exception {
    RepositoryConnection connection = doGetConnection();
    for(Graph graph : items) {
      for(Statement s : graph) {
        connection.add(s);
      }
    }
    connection.close();
  }

  public RepositoryConnection doGetConnection() throws RepositoryException {
    return repository.getConnection();
  }
}
