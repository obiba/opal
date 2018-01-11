/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;


import org.obiba.magma.VariableEntity;
import org.rosuda.REngine.REXP;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralize the value set extractions from the tibble.
 */
class RValueSetFetcher {

  private final RValueTable table;

  RValueSetFetcher(RValueTable table) {
    this.table = table;
  }

  REXP getREXP(VariableEntity entity) {
    // subset tibble: get the row(s) matching the entity id (result is a tibble)
    String cmd = String.format("`%s`[`%s`$`%s`=='%s',]", table.getSymbol(), table.getSymbol(),
        table.getIdColumn(), entity.getIdentifier());
    return table.execute(cmd);
  }

  REXP getREXP(List<VariableEntity> entities) {
    String ids = entities.stream().map(e -> e.getIdentifier()).collect(Collectors.joining("','", "'", "'"));
    // subset tibble: get the row(s) matching the entity ids (result is a tibble)
    String cmd = String.format("`%s`[`%s`$`%s`%%in%% c(%s),]", table.getSymbol(), table.getSymbol(),
        table.getIdColumn(), ids);
    return table.execute(cmd);
  }

}
