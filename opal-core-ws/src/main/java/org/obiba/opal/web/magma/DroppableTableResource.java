/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;

/**
 * A table resource that supports DELETE (drop)
 */
public class DroppableTableResource extends TableResource {

  @Nullable
  private final Set<ValueTableUpdateListener> tableListeners;

  public DroppableTableResource(ValueTable valueTable, Set<Locale> locales, ImportService importService,
      OpalSearchService opalSearchService, StatsIndexManager statsIndexManager, ElasticSearchProvider esProvider,
      @Nullable Set<ValueTableUpdateListener> tableListeners) {
    super(valueTable, locales, importService, opalSearchService, statsIndexManager, esProvider);
    this.tableListeners = tableListeners;
  }

  @DELETE
  public Response drop() {
    if(tableListeners != null && !tableListeners.isEmpty()) {
      for(ValueTableUpdateListener listener : tableListeners) {
        listener.onDelete(getValueTable());
      }
    }
    getDatasource().dropTable(getValueTable().getName());
    return Response.ok().build();
  }
}
