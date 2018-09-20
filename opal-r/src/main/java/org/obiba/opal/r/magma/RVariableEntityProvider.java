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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityProvider;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPVector;

import java.util.Map;
import java.util.Set;

/**
 * The R entities are provided by the id column in the tibble.
 */
class RVariableEntityProvider implements VariableEntityProvider {

  private RValueTable valueTable;

  private final String entityType;

  private String idColumn;

  private Set<VariableEntity> entities;

  private Map<String, RVariableEntity> entitiesMap;

  private boolean multilines = false;

  RVariableEntityProvider(RValueTable valueTable, String entityType, String idColumn) {
    this.valueTable = valueTable;
    this.entityType = Strings.isNullOrEmpty(entityType) ? "Participant" : entityType;
    this.idColumn = idColumn;
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
  public Set<VariableEntity> getVariableEntities() {
    if (entities == null || entities.isEmpty()) {
      entities = Sets.newLinkedHashSet();
      entitiesMap = Maps.newHashMap();
      initialiseIdColumn();
      REXP idVector = valueTable.execute(String.format("`%s`$`%s`", valueTable.getSymbol(), idColumn));
      boolean isNumeric = idVector.isNumeric();
      if (idVector instanceof REXPVector) {
        int length = ((REXPVector)idVector).length();
        try {
          for (String id : idVector.asStrings()) {
            RVariableEntity e = new RVariableEntity(entityType, id, isNumeric);
            entities.add(e);
            entitiesMap.put(e.getIdentifier(), e);
          }
        } catch (REXPMismatchException e) {
          // ignore
        }
        multilines = length > entities.size();
      }
    }
    return entities;
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
