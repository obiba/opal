/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.search;

import org.obiba.opal.spi.search.support.ItemResultDtoStrategy;
import org.obiba.opal.web.model.Search;
import org.obiba.plugins.spi.ServicePlugin;

import java.util.concurrent.ThreadFactory;

@Deprecated
public interface SearchService extends ServicePlugin {

  //
  // Configuration methods
  //

  void configure(SearchSettings settings, VariableSummaryHandler variableSummaryHandler, ThreadFactory threadFactory);

  boolean isEnabled();

  //
  // Index methods
  //

  VariablesIndexManager getVariablesIndexManager();

  ValuesIndexManager getValuesIndexManager();

  //
  // Search methods
  //

  void executeIdentifiersQuery(QuerySettings querySettings, String searchPath, HitsQueryCallback<String> callback) throws SearchException;

  Search.EntitiesResultDto.Builder executeEntitiesQuery(QuerySettings querySettings, String searchPath, String entityType, String query) throws SearchException;

  Search.QueryResultDto executeQuery(QuerySettings querySettings, String searchPath, ItemResultDtoStrategy strategy) throws SearchException;

  Search.QueryResultDto executeQuery(String datasource, String table, Search.QueryTermDto queryDto) throws SearchException;

  Search.QueryResultDto executeQuery(String datasource, String table, Search.QueryTermsDto queryDto) throws SearchException;

  //
  // Search callbacks
  //

  interface HitsQueryCallback<T> {

    void onTotal(int total);

    void onIdentifier(T id);

  }
}
