/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service.impl;

import org.obiba.opal.search.service.QuerySettings;
import org.obiba.opal.search.service.SearchException;
import org.obiba.opal.search.service.SearchQueryExecutor;
import org.obiba.opal.web.model.Search;

public class VariablesQueryExecutor implements SearchQueryExecutor {
  @Override
  public Search.QueryResultDto execute(QuerySettings querySettings) throws SearchException {
    return Search.QueryResultDto.newBuilder().setTotalHits(0).build();
  }
}
