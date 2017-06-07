/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import com.google.common.base.Strings;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.EsResultConverter;
import org.obiba.opal.web.search.support.QuerySearchJsonBuilder;
import org.obiba.opal.web.ws.SortDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base class for searching in variable or value indices.
 */
public abstract class AbstractSearchUtility {

  protected static final String DEFAULT_SORT_FIELD = "_score";

  protected static final String INDEX_FIELD = "index";

  @Autowired
  protected OpalSearchService opalSearchService;

  abstract protected String getSearchPath();

  protected Search.QueryResultDto convertResponse(JSONObject jsonResponse) throws JSONException {
    return new EsResultConverter().convert(jsonResponse);
  }

  protected QuerySearchJsonBuilder buildQuerySearch(String query, int offset, int limit, Collection<String> fields,
      Collection<String> facets, String sortField, String sortDir) {

    Collection<String> safeFields = fields == null ? new ArrayList<String>() : fields;
    addDefaultFields(safeFields);
    QuerySearchJsonBuilder jsonBuilder = new QuerySearchJsonBuilder();
    jsonBuilder.query(query).fields(safeFields).facets(facets).from(offset).size(limit) //
        .sortField(Strings.isNullOrEmpty(sortField) ? DEFAULT_SORT_FIELD : sortField) //
        .sortDir(Strings.isNullOrEmpty(sortDir) ? SortDir.DESC.toString() : sortDir);

    return jsonBuilder;
  }

  protected JSONObject executeQuery(JSONObject jsonQuery) throws JSONException {
    return opalSearchService.executeQuery(jsonQuery, getSearchPath());
  }

  protected boolean searchServiceAvailable() {
    return opalSearchService.isRunning() && opalSearchService.isEnabled();
  }

  protected void addDefaultFields(Collection<String> fields) {
    if(!fields.contains(INDEX_FIELD)) fields.add(INDEX_FIELD);
  }
}

