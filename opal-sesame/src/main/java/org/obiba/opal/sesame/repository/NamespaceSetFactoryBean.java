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

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.springframework.beans.factory.FactoryBean;

/**
 *
 */
public class NamespaceSetFactoryBean implements FactoryBean {

  private Repository repository;

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public Object getObject() throws Exception {
    if(repository == null) throw new IllegalStateException("repository cannot be null");

    RepositoryConnection connection = repository.getConnection();
    try {
      RepositoryResult<Namespace> result = connection.getNamespaces();
      try {
        Set<Namespace> namespaces = new HashSet<Namespace>();
        while(result.hasNext()) {
          Namespace ns = (Namespace) result.next();
          namespaces.add(ns);
        }
        return namespaces;
      } finally {
        if(result != null) {
          result.close();
        }
      }
    } finally {
      if(connection != null) {
        connection.close();
      }
    }
  }

  public Class getObjectType() {
    return Set.class;
  }

  public boolean isSingleton() {
    return false;
  }

}
