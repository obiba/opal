/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RServerResult;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/**
 * The value set is the tibble subset for the entity identifier.
 */
class RValueSet extends ValueSetBean {

  private final RValueSetFetcher fetcher;

  private Map<Integer, List<Object>> columnValues;

  RValueSet(@NotNull RValueTable table, @NotNull VariableEntity entity) {
    super(table, entity);
    this.fetcher = new RValueSetFetcher(table);
  }

  public Map<Integer, List<Object>> getValuesByPosition() {
    if (columnValues == null) {
      parseResult(fetcher.getResult(getVariableEntity()));
    }
    return columnValues;
  }

  /**
   * Parse the tibble to extract values that are related to the entity (could be multilines).
   *
   * @param result
   */
  public void parseResult(RServerResult result) {
    columnValues = Maps.newHashMap();
    if (result.isList()) {
      List<RServerResult> list = result.asList();

      if (list.stream().anyMatch(RServerResult::isNamedList)) {
        // results from rock are one JSON object per row
        Map<String, Integer> colPositions = getRValueTable().getColumnPositions();
        for (RServerResult rowResult : list) {
          RNamedList<RServerResult> rowNamedResults = rowResult.asNamedList();
          String id = rowNamedResults.get(getRValueTable().getIdColumn()).asStrings()[0];
          if (getVariableEntity().getIdentifier().equals(id)) {
            Map<String, Object> rowMap = asMapOfObjects(rowResult);
            for (String colName : rowMap.keySet()) {
              int colPos = colPositions.get(colName);
              if (!columnValues.containsKey(colPos))
                columnValues.put(colPos, Lists.newArrayList());
              columnValues.get(colPos).add(rowMap.get(colName));
            }
          }
        }
      } else {
        // results from Rserve are column vectors
        String[] ids = list.get(getIdPosition()).asStrings();
        List<Integer> rowIdx = Lists.newArrayList();
        int row = 0;
        for (String id : ids) {
          if (getRVariableEntity().equals(new RVariableEntity(getRValueTable().getEntityType(), id))) {
            rowIdx.add(row);
          }
          row++;
        }

        for (int col = 0; col < list.size(); col++) {
          if (getIdPosition() == col) continue;
          columnValues.put(col, Lists.newArrayList());
          RServerResult vector = list.get(col);
          boolean[] nas = vector.isNA();
          Object[] objectValues = asArrayOfObjects(vector);
          // #3303 force NA representation
          for (int i = 0; i < nas.length; i++) {
            if (nas[i]) objectValues[i] = null;
          }
          for (int r = 0; r < objectValues.length; r++) {
            if (rowIdx.contains(r)) {
              columnValues.get(col).add(objectValues[r]);
            }
          }
        }
      }
    }
  }

  private Map<String, Object> asMapOfObjects(RServerResult vector) {
    return (Map<String, Object>) vector.asNativeJavaObject();
  }

  private Object[] asArrayOfObjects(RServerResult vector) {
    Object payload = vector.asNativeJavaObject();
    int arrlength = Array.getLength(payload);
    Object[] outputArray = new Object[arrlength];
    for (int i = 0; i < arrlength; ++i) {
      outputArray[i] = Array.get(payload, i);
    }
    return outputArray;
  }

  private RVariableEntity getRVariableEntity() {
    return getRValueTable().getRVariableEntity(getVariableEntity());
  }

  private int getIdPosition() {
    return getRValueTable().getIdPosition();
  }

  private RValueTable getRValueTable() {
    return (RValueTable) getValueTable();
  }

}
