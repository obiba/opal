/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.report;

import org.openrdf.model.Namespace;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * 
 */
public class QueryUtil {

  public static void prefixQuery(RepositoryConnection connection, StringBuilder query) {
    StringBuilder sb = new StringBuilder();
    RepositoryResult<Namespace> result = null;

    try {
      result = connection.getNamespaces();
      while(result.hasNext()) {
        Namespace ns = result.next();
        sb.append("PREFIX ").append(ns.getPrefix()).append(":<").append(ns.getName()).append(">\n");
      }
      query.insert(0, sb);
    } catch(RepositoryException e) {
      throw new RuntimeException(e);
    } finally {
      if(result != null) {
        try {
          result.close();
        } catch(RepositoryException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
