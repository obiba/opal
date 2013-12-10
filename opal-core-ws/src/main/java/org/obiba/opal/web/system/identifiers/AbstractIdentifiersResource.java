/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.identifiers;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.service.IdentifiersTableService;

public abstract class AbstractIdentifiersResource {

  protected abstract IdentifiersTableService getIdentifiersTableService();

  /**
   * Get the identifiers value table of the given entity type, case insensitive.
   *
   * @param entityType
   * @return
   */
  @Nullable
  protected ValueTable getValueTable(@NotNull String entityType) {
    for(ValueTable table : getDatasource().getValueTables()) {
      if(table.getEntityType().toLowerCase().equals(entityType.toLowerCase())) {
        return table;
      }
    }
    return null;
  }

  protected Datasource getDatasource() {
    return getIdentifiersTableService().getDatasource();
  }

}
