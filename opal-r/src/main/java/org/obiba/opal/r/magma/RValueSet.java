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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * The value set is the tibble subset for the entity identifier.
 */
class RValueSet extends ValueSetBean {

  private final RValueSetFetcher fetcher;

  private Map<Integer, List<String>> columnValues;

  RValueSet(@NotNull RValueTable table, @NotNull VariableEntity entity) {
    super(table, entity);
    this.fetcher = new RValueSetFetcher(table);
  }

  public Map<Integer, List<String>> getValuesByPosition() {
    if (columnValues == null) {
      parseREXP(fetcher.getREXP(getVariableEntity()));
    }
    return columnValues;
  }

  /**
   * Parse the tibble to extract values that are related to the entity (could be multilines).
   *
   * @param rexp
   */
  public void parseREXP(REXP rexp) {
    columnValues = Maps.newHashMap();
    if (rexp instanceof REXPGenericVector) {
      try {
        REXPGenericVector tibble = (REXPGenericVector) rexp;
        RList vectors = tibble.asList();
        REXP vectorId = (REXP) vectors.get(getIdPosition() - 1);
        String[] ids = vectorId.asStrings();
        List<Integer> rowIdx = Lists.newArrayList();
        int row = 0;
        for (String id : ids) {
          if (getVariableEntity().getIdentifier().equals(RVariableEntityProvider.normalizeId(id))) {
            rowIdx.add(row);
          }
          row++;
        }
        for (int col = 0; col<vectors.size(); col++) {
          if (getIdPosition() == col + 1) continue;
          int position = col + 1;
          columnValues.put(position, Lists.newArrayList());
          REXP vector = (REXP) vectors.get(col);
          String[] strValues = vector.asStrings();
          for (int r = 0; r < strValues.length; r++) {
            if (rowIdx.contains(r)) {
              columnValues.get(position).add(strValues[r]);
            }
          }
        }
      } catch (REXPMismatchException e) {
        // ignore
      }
    }
  }

  private int getIdPosition() {
    return getRValueTable().getIdPosition();
  }

  private RValueTable getRValueTable() {
    return (RValueTable) getValueTable();
  }

}
