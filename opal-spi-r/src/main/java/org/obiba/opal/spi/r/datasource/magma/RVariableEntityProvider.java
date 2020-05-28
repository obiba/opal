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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityProvider;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPVector;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

/**
 * The R entities are provided by the id column in the tibble.
 */
class RVariableEntityProvider implements VariableEntityProvider {

  private RValueTable valueTable;

  private final String entityType;

  private final NumberFormat fmt = NumberFormat.getInstance();

  private String idColumn;

  private List<VariableEntity> entities;

  private Map<String, RVariableEntity> entitiesMap;

  private boolean multilines = false;

  RVariableEntityProvider(RValueTable valueTable, String entityType, String idColumn) {
    this.valueTable = valueTable;
    this.entityType = Strings.isNullOrEmpty(entityType) ? "Participant" : entityType;
    this.idColumn = idColumn;
    fmt.setGroupingUsed(false);
    fmt.setMaximumIntegerDigits(999);
    fmt.setMaximumFractionDigits(999);
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return entityType.equals(entityType);
  }

  @Override
  public List<VariableEntity> getVariableEntities() {
    if (entities == null || entities.isEmpty()) {
      entities = Lists.newArrayList();
      entitiesMap = Maps.newHashMap();
      initialiseIdColumn();
      REXP idVector = valueTable.execute(String.format("`%s`$`%s`", valueTable.getSymbol(), idColumn));
      boolean isNumeric = idVector.isNumeric();
      if (idVector instanceof REXPVector) {
        int length = ((REXPVector) idVector).length();
        try {
          if (isNumeric) {
            for (double id : idVector.asDoubles()) {
              registerEntity(new RVariableEntity(entityType, id));
            }
          } else {
            for (String id : idVector.asStrings()) {
              registerEntity(new RVariableEntity(entityType, id));
            }
          }
        } catch (REXPMismatchException e) {
          // ignore
        }
        multilines = length > entities.size();
      }
    }
    return entities;
  }

  private void registerEntity(RVariableEntity e) {
    if (entitiesMap.containsKey(e.getIdentifier())) return;
    entities.add(e);
    entitiesMap.put(e.getIdentifier(), e);
  }

  public RVariableEntity getRVariableEntity(VariableEntity entity) {
    return entitiesMap.get(entity.getIdentifier());
  }

  String getIdColumn() {
    initialiseIdColumn();
    return idColumn;
  }

  boolean isMultilines() {
    // make sure it was initialized
    getVariableEntities();
    return multilines;
  }

  private void initialiseIdColumn() {
    if (Strings.isNullOrEmpty(idColumn)) {
      REXPVector colnames = (REXPVector) valueTable.execute(String.format("colnames(`%s`)", valueTable.getSymbol()));
      try {
        idColumn = colnames.asStrings()[0];
      } catch (REXPMismatchException e) {
        idColumn = RDatasource.DEFAULT_ID_COLUMN_NAME;
      }
    }
  }
}
