/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.datasource.csv.CsvValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.opal.web.model.Magma.ConflictDto;
import org.obiba.opal.web.model.Magma.DatasourceCompareDto;
import org.obiba.opal.web.model.Magma.TableCompareDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;

@NoAuthorization
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class CompareResourceImpl implements CompareResource {

  private static final Logger log = LoggerFactory.getLogger(CompareResourceImpl.class);

  private static final String INCOMPATIBLE_ENTITY_TYPE = "IncompatibleEntityType";

  private static final String INCOMPATIBLE_VALUE_TYPE = "IncompatibleValueType";

  private static final String CSV_VARIABLE_MISSING = "CsvVariableMissing";

  private static final int INITIAL_CAPACITY = 5000;

  private Datasource comparedDatasource;

  private ValueTable comparedTable;

  @Override
  public void setComparedDatasource(Datasource comparedDatasource) {
    this.comparedDatasource = comparedDatasource;
  }

  @Override
  public void setComparedTable(ValueTable comparedTable) {
    this.comparedTable = comparedTable;
  }

  @Override
  public Response compare(String with) {
    if(comparedDatasource != null) {
      Datasource withDatasource = getDatasource(with);
      DatasourceCompareDto dto = createDatasourceCompareDto(comparedDatasource, withDatasource);
      return Response.ok().entity(dto).build();
    }
    if(comparedTable != null) {
      ValueTable withTable = getValueTable(with);
      TableCompareDto dto = createTableCompareDto(comparedTable, withTable);
      return Response.ok().entity(dto).build();
    }
    log.error("Cannot compare because comparedDatasource and comparedTable are both null.");
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  @VisibleForTesting
  Datasource getDatasource(String datasourceName) {
    return MagmaEngine.get().getDatasource(datasourceName);
  }

  @VisibleForTesting
  ValueTable getValueTable(String fqTableName) {
    String datasourceName = MagmaEngineTableResolver.valueOf(fqTableName).getDatasourceName();
    String tableName = MagmaEngineTableResolver.valueOf(fqTableName).getTableName();
    return MagmaEngine.get().getDatasource(datasourceName).getValueTable(tableName);
  }

  private DatasourceCompareDto createDatasourceCompareDto(Datasource compared, Datasource with) {
    DatasourceCompareDto.Builder dtoBuilder = DatasourceCompareDto.newBuilder() //
        .setCompared(Dtos.asDto(compared)) //
        .setWithDatasource(Dtos.asDto(with));

    for(ValueTable vt : compared.getValueTables()) {
      TableCompareDto tableCompareDto = with.hasValueTable(vt.getName()) //
          ? createTableCompareDto(vt, with.getValueTable(vt.getName())) //
          : createTableCompareDtoWhereSecondTableDoesNotExist(vt);
      dtoBuilder.addTableComparisons(tableCompareDto);
    }

    return dtoBuilder.build();
  }

  private TableCompareDto createTableCompareDto(ValueTable compared, ValueTable with) {
    Set<Variable> variablesInCompared = asSet(compared.getVariables());
    Set<Variable> variablesInWith = asSet(with.getVariables());

    Collection<Variable> newVariables = new LinkedHashSet<>(variablesInCompared);
    newVariables.removeAll(variablesInWith);

    Collection<Variable> missingVariables = new LinkedHashSet<>(variablesInWith);
    missingVariables.removeAll(variablesInCompared);

    Collection<Variable> existingVariables = new LinkedHashSet<>(variablesInCompared);
    existingVariables.retainAll(variablesInWith);

    return createTableCompareDto(compared, with, newVariables, missingVariables, existingVariables);
  }

  private TableCompareDto createTableCompareDtoWhereSecondTableDoesNotExist(ValueTable compared) {
    Set<Variable> variablesInCompared = asSet(compared.getVariables());
    Iterable<Variable> newVariables = new LinkedHashSet<>(variablesInCompared);
    Iterable<Variable> missingVariables = new LinkedHashSet<>(INITIAL_CAPACITY);
    Iterable<Variable> existingVariables = new LinkedHashSet<>(INITIAL_CAPACITY);

    return createTableCompareDto(compared, null, newVariables, missingVariables, existingVariables);
  }

  private TableCompareDto createTableCompareDto(ValueTable compared, ValueTable with, Iterable<Variable> newVariables,
      Iterable<Variable> missingVariables, Iterable<Variable> existingVariables) {
    TableCompareDto.Builder dtoBuilder = TableCompareDto.newBuilder();
    dtoBuilder.setCompared(Dtos.asDto(compared, true, false));

    if(with != null) {
      dtoBuilder.setWithTable(Dtos.asDto(with, true, false));
    }

    Collection<ConflictDto> conflicts = new LinkedHashSet<>(INITIAL_CAPACITY);
    conflicts.addAll(getMissingCsvVariableConflicts(compared));

    conflicts.addAll(getConflicts(compared, with, existingVariables, false));
    conflicts.addAll(getConflicts(compared, with, newVariables, true));
    dtoBuilder.addAllConflicts(conflicts);

    for(Variable v : getUnconflicting(newVariables, conflicts)) {
      dtoBuilder.addNewVariables(Dtos.asDto(v));
    }
    for(Variable v : missingVariables) {
      dtoBuilder.addMissingVariables(Dtos.asDto(v));
    }

    return addTableCompareDtoModifications(dtoBuilder, with, existingVariables, conflicts).build();
  }

  private TableCompareDto.Builder addTableCompareDtoModifications(TableCompareDto.Builder dtoBuilder, ValueTable with,
      Iterable<Variable> existingVariables, Iterable<ConflictDto> conflicts) {
    Set<Variable> unconflicting = getUnconflicting(existingVariables, conflicts);
    if(with != null) {
      Set<Variable> modifiedVariables = getUnconflictingModified(with, unconflicting);
      for(Variable variable : modifiedVariables) {
        dtoBuilder.addModifiedVariables(Dtos.asDto(variable));
      }
      unconflicting.removeAll(modifiedVariables);
    }
    for(Variable variable : unconflicting) {
      dtoBuilder.addUnmodifiedVariables(Dtos.asDto(variable));
    }

    return dtoBuilder;
  }

  private Collection<ConflictDto> getMissingCsvVariableConflicts(ValueTable compared) {
    Collection<ConflictDto> conflicts = new LinkedHashSet<>(INITIAL_CAPACITY);
    if(compared.getDatasource().getType().equals(CsvDatasource.TYPE)) {
      // support IncrementalView wrapping compared table
      CsvValueTable csvValueTable = (CsvValueTable) (compared instanceof ValueTableWrapper //
          ? ((ValueTableWrapper) compared).getInnermostWrappedValueTable() //
          : compared);
      for(Variable missingVariable : csvValueTable.getMissingVariables()) {
        conflicts
            .add(createConflictDto(Dtos.asDto(missingVariable).setIsNewVariable(true).build(), CSV_VARIABLE_MISSING));
      }
    }
    return conflicts;
  }

  private Collection<ConflictDto> getConflicts(ValueTable compared, ValueTable with, Iterable<Variable> variables,
      boolean newVariable) {
    String entityType = null;
    Collection<ConflictDto> conflicts = new LinkedHashSet<>(INITIAL_CAPACITY);
    for(Variable variable : variables) {
      if(entityType == null) {
        entityType = variable.getEntityType();
      }
      getVariableConflicts(compared, with, newVariable, conflicts, entityType, variable);
    }
    return conflicts;
  }

  private void getVariableConflicts(ValueTable compared, ValueTable with, boolean newVariable,
      Collection<ConflictDto> conflicts, String entityType, Variable variable) {
    if(with == null) {
      // Target (with) will be created
      if(!entityType.equals(variable.getEntityType())) {
        conflicts.add(
            createConflictDto(Dtos.asDto(variable).setIsNewVariable(true).build(), INCOMPATIBLE_ENTITY_TYPE, entityType,
                variable.getEntityType()));
      }
    } else {
      // Target (with) table already exist
      String name = variable.getName();
      Variable variableInCompared = compared.getVariable(name);
      if(!variableInCompared.getEntityType().equals(with.getEntityType())) {
        conflicts.add(
            createConflictDto(Dtos.asDto(variable).setIsNewVariable(newVariable).build(), INCOMPATIBLE_ENTITY_TYPE,
                variableInCompared.getEntityType(), with.getEntityType()));
      }
      try {
        Variable variableInWith = with.getVariable(name);
        if(!variableInCompared.getValueType().equals(variableInWith.getValueType()) && !with.isView()) {
          conflicts.add(
              createConflictDto(Dtos.asDto(variable).setIsNewVariable(newVariable).build(), INCOMPATIBLE_VALUE_TYPE,
                  variableInCompared.getValueType().getName(), variableInWith.getValueType().getName()));
        }
      } catch(NoSuchVariableException variableDoesNotExist) {
        // Case where the variable does not exist in Opal but its destination table already exist.
      }
    }
  }

  private Set<Variable> getUnconflicting(Iterable<Variable> variables, Iterable<ConflictDto> conflicts) {
    Set<Variable> unconflicting = new LinkedHashSet<>(INITIAL_CAPACITY);

    Collection<String> conflicting = new LinkedHashSet<>(INITIAL_CAPACITY);
    for(ConflictDto dto : conflicts) {
      conflicting.add(dto.getVariable().getName());
    }

    for(Variable v : variables) {
      if(!conflicting.contains(v.getName())) {
        unconflicting.add(v);
      }
    }

    return unconflicting;
  }

  private Set<Variable> getUnconflictingModified(ValueTable with, Iterable<Variable> variables) {
    Set<Variable> modified = new LinkedHashSet<>(INITIAL_CAPACITY);

    for(Variable v : variables) {
      Variable withVar = with.getVariable(v.getName());
      if(isModified(v, withVar)) {
        modified.add(v);
      }
    }

    return modified;
  }

  private boolean isModified(Variable compared, Variable with) {
    return isModified(compared.getMimeType(), with.getMimeType()) ||
        isModified(compared.getOccurrenceGroup(), with.getOccurrenceGroup()) ||
        isModified(compared.getReferencedEntityType(), with.getReferencedEntityType()) ||
        isModified(compared.getUnit(), with.getUnit()) ||
        areCategoriesModified(compared.getCategories(), with.getCategories()) ||
        areAttributesModified(compared.getAttributes(), with.getAttributes());
  }

  @SuppressWarnings("SimplifiableIfStatement")
  private boolean isModified(@Nullable String compared, @Nullable String with) {
    if(compared == null && with == null) return false;
    if((compared == null || compared.isEmpty()) && (with == null || with.isEmpty())) return false;
    return !(compared != null && compared.equals(with.replace("\r", ""));
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private boolean areCategoriesModified(Collection<Category> compared, Collection<Category> with) {
    if(compared == null && with == null) return false;
    if((compared == null || compared.isEmpty()) && (with == null || with.isEmpty())) return false;
    if(compared == null && with != null || compared != null && with == null) return true;
    if(compared != null && with != null && compared.size() != with.size()) return true;

    if(compared != null && with != null) {
      for(Category comparedCat : compared) {
        boolean found = false;
        for(Category withCat : with) {
          if(comparedCat.getName().equals(withCat.getName())) {
            if(isModified(comparedCat, withCat)) return true;
            found = true;
          }
        }
        if(!found) return true;
      }
    }
    return false;
  }

  private boolean isModified(Category compared, Category with) {
    return compared.isMissing() != with.isMissing() ||
        areAttributesModified(compared.getAttributes(), with.getAttributes());
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private boolean areAttributesModified(Collection<Attribute> compared, Collection<Attribute> with) {
    if(compared == null && with == null) return false;
    if((compared == null || compared.isEmpty()) && (with == null || with.isEmpty())) return false;
    if(compared == null || with == null) return true;
    if(compared.size() != with.size()) return true;

    for(Attribute comparedAttr : compared) {
      boolean found = false;
      for(Attribute withAttr : with) {
        if(isSameAttribute(comparedAttr, withAttr)) {
          if(isModified(comparedAttr.getValue(), withAttr.getValue())) return true;
          found = true;
        }
      }
      if(!found) return true;
    }
    return false;
  }

  private boolean isModified(Value compared, Value with) {
    if(compared == null && with == null) return false;
    String comparedStr = compared == null || compared.isNull() ? null : compared.toString();
    String withStr = with == null || with.isNull() ? null : with.toString();
    return isModified(comparedStr, withStr);
  }

  private boolean isSameAttribute(Attribute compared, Attribute with) {
    if(!compared.getName().equals(with.getName())) return false;
    Locale comparedLocale = compared.isLocalised() ? compared.getLocale() : null;
    Locale withLocale = with.isLocalised() ? with.getLocale() : null;
    return Objects.equals(comparedLocale, withLocale);
  }

  private ConflictDto createConflictDto(VariableDto variableDto, String code, String... args) {
    ConflictDto.Builder dtoBuilder = ConflictDto.newBuilder();
    dtoBuilder.setVariable(variableDto);
    dtoBuilder.setCode(code);
    for(String arg : args) {
      dtoBuilder.addArguments(arg);
    }
    return dtoBuilder.build();
  }

  private <T> Set<T> asSet(Iterable<T> iterable) {
    Set<T> set = new LinkedHashSet<>(INITIAL_CAPACITY);
    for(T elem : iterable) {
      set.add(elem);
    }
    return set;
  }
}

