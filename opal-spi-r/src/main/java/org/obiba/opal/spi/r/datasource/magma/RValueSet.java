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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RServerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The value set is the tibble subset for the entity identifier.
 */
class RValueSet extends ValueSetBean {

  private static final Logger log = LoggerFactory.getLogger(RValueSet.class);

  private final RValueSetFetcher fetcher;

  private Map<Integer, List<Object>> columnValues;

  RValueSet(@NotNull TibbleTable table, @NotNull VariableEntity entity) {
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
        Map<String, Integer> colPositions = getTibbleTable().getColumnPositions();
        for (RServerResult rowResult : list) {
          RNamedList<RServerResult> rowNamedResults = rowResult.asNamedList();
          String id = rowNamedResults.get(getTibbleTable().getIdColumn()).asStrings()[0];
          if (getVariableEntity().getIdentifier().equals(id)) {
            Map<String, Object> rowMap = asMapOfObjects(rowResult);
            for (String colName : colPositions.keySet()) {
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
          if (getRVariableEntity().equals(new RVariableEntity(getTibbleTable().getEntityType(), id))) {
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
    } else {
      log.warn("Not the expected jsonlite's tibble serialization format, suspecting a C stack usage limit issue in R server");
      if (result.isNamedList()) {
        // expecting a data frame structure
        RNamedList<RServerResult> dataFrame = result.asNamedList();
        List<RServerResult> vectors = dataFrame.get("value").asList();

        String[] ids = vectors.get(getIdPosition()).asNamedList().get("value").asStrings();
        List<Integer> rowIdx = Lists.newArrayList();
        int row = 0;
        for (String id : ids) {
          if (getRVariableEntity().equals(new RVariableEntity(getTibbleTable().getEntityType(), id))) {
            rowIdx.add(row);
          }
          row++;
        }

        for (int col = 0; col < vectors.size(); col++) {
          if (getIdPosition() == col) continue;
          columnValues.put(col, Lists.newArrayList());
          RServerResult vector = vectors.get(col).asNamedList().get("value");
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
    Object[] outputArray;
    if (payload instanceof List) {
      List<Object> list = (List) payload;
      outputArray = new Object[list.size()];
      list.toArray(outputArray);
    } else {
      int arrlength = Array.getLength(payload);
      outputArray = new Object[arrlength];
      for (int i = 0; i < arrlength; ++i) {
        outputArray[i] = Array.get(payload, i);
      }
    }
    return outputArray;
  }

  private RVariableEntity getRVariableEntity() {
    return getTibbleTable().getRVariableEntity(getVariableEntity());
  }

  private int getIdPosition() {
    return getTibbleTable().getIdPosition();
  }

  private TibbleTable getTibbleTable() {
    return (TibbleTable) getValueTable();
  }

}
