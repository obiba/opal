/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.search;

import org.obiba.opal.web.model.Search;

/**
 * Interface for executing queries using @{Search.QueryTermsDto} or @{Search.QueryTermDto}.
 */
public interface SearchQueryExecutor {

  /**
   * Execute a query terms search query.
   *
   * @param dtoQueries
   * @return
   * @throws SearchException
   */
  Search.QueryResultDto execute(Search.QueryTermsDto dtoQueries) throws SearchException;

  /**
   * Execute a query term search query.
   *
   * @param dtoQuery
   * @return
   * @throws SearchException
   */
  Search.QueryResultDto execute(Search.QueryTermDto dtoQuery) throws SearchException;

}
