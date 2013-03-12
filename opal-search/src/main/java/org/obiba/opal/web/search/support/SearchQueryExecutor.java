/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import org.codehaus.jettison.json.JSONException;
import org.obiba.opal.web.model.Search;

/**
 * Interface for executing queries using @{Search.QueryTermsDto} or @{Search.QueryTermDto}.
 */
public interface SearchQueryExecutor {

  /**
   * Execute a query terms search query.
   *
   * @param indexManagerHelper
   * @param dtoQueries
   * @return
   * @throws JSONException
   */
  Search.QueryResultDto execute(IndexManagerHelper indexManagerHelper, Search.QueryTermsDto dtoQueries)
      throws JSONException;

  /**
   * Execute a query term search query.
   *
   * @param indexManagerHelper
   * @param dtoQuery
   * @return
   * @throws JSONException
   */
  Search.QueryResultDto execute(IndexManagerHelper indexManagerHelper, Search.QueryTermDto dtoQuery)
      throws JSONException;
}
