/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service;

import org.obiba.opal.web.model.Search;

public interface SearchQueryExecutor {

  /**
   * Performs the query.
   *
   * @param querySettings
   * @return
   * @throws SearchException
   */
  Search.QueryResultDto execute(QuerySettings querySettings) throws SearchException;

  /**
   * Performs a count on the query.
   *
   * @param querySettings
   * @return
   * @throws SearchException
   */
  Search.QueryCountDto count(QuerySettings querySettings) throws SearchException;

}
