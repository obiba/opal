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
import com.google.common.collect.Lists;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.spi.search.QuerySettings;
import org.obiba.opal.web.ws.SortDir;
import org.springframework.beans.factory.annotation.Autowired;

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

  protected QuerySettings buildQuerySearch(String query, int offset, int limit, Collection<String> fields,
                                           Collection<String> facets, String sortField, String sortDir) {

    Collection<String> safeFields = fields == null ? Lists.newArrayList() : fields;
    addDefaultFields(safeFields);
    QuerySettings querySettings = QuerySettings.newSettings(query)
        .fields(safeFields).facets(facets).from(offset).size(limit) //
        .sortField(Strings.isNullOrEmpty(sortField) ? DEFAULT_SORT_FIELD : sortField) //
        .sortDir(Strings.isNullOrEmpty(sortDir) ? SortDir.DESC.toString() : sortDir);

    return querySettings;
  }

  protected boolean searchServiceAvailable() {
    return opalSearchService.isRunning() && opalSearchService.isEnabled();
  }

  protected void addDefaultFields(Collection<String> fields) {
    if (!fields.contains(INDEX_FIELD)) fields.add(INDEX_FIELD);
  }
}

