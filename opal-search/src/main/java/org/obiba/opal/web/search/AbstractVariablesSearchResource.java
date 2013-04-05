/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import java.util.Collection;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.rest.RestRequest;
import org.obiba.opal.search.VariablesIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.EsQueryExecutor;
import org.obiba.opal.web.search.support.EsResultConverter;
import org.obiba.opal.web.search.support.QuerySearchJsonBuilder;
import org.obiba.opal.web.ws.SortDir;

import com.google.common.base.Strings;

public abstract class AbstractVariablesSearchResource {

  protected static final String DEFAULT_SORT_FIELD = "_score";

  protected static final String INDEX_FIELD = "index";

  protected final OpalSearchService opalSearchService;

  protected final ElasticSearchProvider esProvider;

  protected final VariablesIndexManager indexManager;

  public AbstractVariablesSearchResource(OpalSearchService service, ElasticSearchProvider provider,
      VariablesIndexManager manager) {
    opalSearchService = service;
    esProvider = provider;
    indexManager = manager;
  }

  //
  // Protected members
  //

  abstract protected String getSearchPath();

  abstract protected Collection<String> getFilterTypes();

  protected Search.QueryResultDto convertResonse(JSONObject jsonResponse) throws JSONException {
    return new EsResultConverter().convert(jsonResponse);
  }

  protected JSONObject executeQuery(String query, int offset, int limit, Collection<String> fields) throws JSONException {
    return executeQuery(query, offset, limit, fields, DEFAULT_SORT_FIELD, SortDir.DESC.toString());
  }

  protected JSONObject executeQuery(String query, int offset, int limit, Collection<String> fields, String sortField,
      String sortDir) throws JSONException {

    addDefaultFields(fields);

    if(Strings.isNullOrEmpty(sortField)) sortField = DEFAULT_SORT_FIELD;
    if(Strings.isNullOrEmpty(sortDir)) sortDir = SortDir.DESC.toString();

    QuerySearchJsonBuilder jsonBuilder = new QuerySearchJsonBuilder();
    JSONObject jsonQuery = jsonBuilder.setQuery(query).setFields(fields).setFrom(offset).setSize(limit)
        .setSortField(sortField).setSortDir(sortDir).setFilterTypes(getFilterTypes()).build();
    EsQueryExecutor queryExecutor = new EsQueryExecutor(esProvider).setSearchPath(getSearchPath());
    return queryExecutor.execute(jsonQuery, RestRequest.Method.POST);
  }

  protected boolean searchServiceAvailable() {
    return opalSearchService.isRunning() && opalSearchService.isEnabled();
  }

  protected void addDefaultFields(Collection<String> fields) {
    if(!fields.contains(INDEX_FIELD)) fields.add(INDEX_FIELD);
  }
}

