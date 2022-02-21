/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;


import org.obiba.magma.VariableEntity;
import org.obiba.opal.spi.r.RServerResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralize the value set extractions from the tibble.
 */
class RValueSetFetcher {

  private final TibbleTable table;

  RValueSetFetcher(TibbleTable table) {
    this.table = table;
  }

  RServerResult getResult(VariableEntity entity) {
    RVariableEntity re = table.getRVariableEntity(entity);
    String rid = re.getRIdentifier();
    if (!re.isNumeric()) rid = String.format("'%s'", rid);
    // subset tibble: get the row(s) matching the entity id (result is a tibble)
    String cmd = String.format("tibble::as_tibble(`%s` %%>%% filter(`%s` == %s))", table.getSymbol(), table.getIdColumn(), rid);
    return table.execute(cmd);
  }

  RServerResult getResult(List<VariableEntity> entities) {
    // to query R, we need the original R entity identifier
    String ids = entities.stream()
        .map(e -> {
          RVariableEntity re = table.getRVariableEntity(e);
          String rid = re.getRIdentifier();
          if (!re.isNumeric()) rid = String.format("'%s'", rid);
          return rid;
        })
        .collect(Collectors.joining(","));
    // subset tibble: get the row(s) matching the entity ids (result is a tibble)
    String cmd = String.format("tibble::as_tibble(`%s` %%>%% filter(`%s` %%in%% c(%s)))", table.getSymbol(), table.getIdColumn(), ids);
    return table.execute(cmd);
  }

}
