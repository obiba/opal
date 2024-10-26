/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.search.service.QuerySettings;
import org.obiba.opal.web.ws.SortDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * Base class for searching in variable or value indices.
 */
public abstract class AbstractSearchUtility {

  protected static final String DEFAULT_SORT_FIELD = "_score";

  @Autowired
  protected OpalSearchService opalSearchService;

  abstract protected String getSearchPath();

  protected QuerySettings buildQuerySearch(String query, String lastDoc, int limit, Collection<String> fields,
                                           Collection<String> facets, String sortField, String sortDir) {
    String sortBy = Strings.isNullOrEmpty(sortField) ? DEFAULT_SORT_FIELD : sortField;
    String sortOrder = Strings.isNullOrEmpty(sortDir) ? SortDir.DESC.toString() : sortDir;
    return buildQuerySearch(query, lastDoc, limit, fields, facets, Lists.newArrayList(sortBy + ":" + sortOrder));
  }

  protected QuerySettings buildQuerySearch(String query, String lastDoc, int limit, Collection<String> fields,
                                           Collection<String> facets, List<String> sortWithOrder) {

    List<String> safeFields = Lists.newArrayList();
    if (fields != null)
      safeFields.addAll(fields);
    QuerySettings querySettings = QuerySettings.newSettings(query)
      .fields(safeFields).facets(facets).size(limit) //
      .sortWithOrder(sortWithOrder);

    if (!Strings.isNullOrEmpty(lastDoc)) {
      querySettings.lastDoc(lastDoc);
    }

    return querySettings;
  }

  protected boolean searchServiceAvailable() {
    return opalSearchService.isRunning() && opalSearchService.isEnabled();
  }

}

