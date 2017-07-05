/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.search;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.opal.spi.ServicePlugin;
import org.obiba.opal.web.model.Search;

import java.util.concurrent.ThreadFactory;

public interface SearchService extends ServicePlugin {

  //
  // Configuration methods
  //

  void configure(SearchSettings settings, VariableSummaryHandler variableSummaryHandler, ThreadFactory threadFactory);

  SearchSettings getConfig();

  boolean isEnabled();

  //
  // Index methods
  //

  VariablesIndexManager getVariablesIndexManager();

  ValuesIndexManager getValuesIndexManager();

  //
  // Search methods
  //

  JSONObject executeQuery(JSONObject jsonQuery, String searchPath) throws JSONException;

  Search.QueryResultDto executeQuery(String datasource, String table, Search.QueryTermDto queryDto) throws JSONException;

  Search.QueryResultDto executeQuery(String datasource, String table, Search.QueryTermsDto queryDto) throws JSONException;
}
