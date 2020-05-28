/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search.support;


import com.google.common.base.Strings;
import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.parser.RQLParser;
import org.obiba.opal.spi.search.ValuesIndexManager;

import java.util.stream.Collectors;

public class RQLParserFactory {

  public static RQLParser newParser() {
    return new RQLParser();
  }

  /**
   * Converts a RQL query to a ES query.
   *
   * @param rqlQuery
   * @param valuesIndexManager
   * @return
   */
  public static String parse(String rqlQuery, ValuesIndexManager valuesIndexManager) {
    String esQuery;
    ASTNode queryNode = newParser().parse(rqlQuery);
    if ("and".equals(queryNode.getName()) || "or".equals(queryNode.getName()) || "".equals(queryNode.getName())) {
      esQuery = queryNode.getArguments().stream()
          .map(qn -> new RQLValueSetVariableCriterionParser(valuesIndexManager, (ASTNode) qn).getQuery())
          .filter(q -> !Strings.isNullOrEmpty(q))
          .collect(Collectors.joining("or".equals(queryNode.getName()) ? " OR " : " AND "));
    } else { // single query
      esQuery = new RQLValueSetVariableCriterionParser(valuesIndexManager, queryNode).getQuery();
    }
    return esQuery;
  }

}
