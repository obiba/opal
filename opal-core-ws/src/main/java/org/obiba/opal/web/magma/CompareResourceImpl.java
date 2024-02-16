/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import com.google.common.annotations.VisibleForTesting;
import org.obiba.magma.*;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.datasource.csv.CsvValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.magma.support.VariableHelper;
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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
  public Response compare(String with, boolean merge) {
    if (comparedDatasource != null) {
      Datasource withDatasource = getDatasource(with);
      DatasourceCompareDto dto = createDatasourceCompareDto(comparedDatasource, withDatasource, merge);
      return Response.ok().entity(dto).build();
    }
    if (comparedTable != null) {
      ValueTable withTable = getValueTable(with);
      TableCompareDto dto = createTableCompareDto(comparedTable, withTable, merge);
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

  private DatasourceCompareDto createDatasourceCompareDto(Datasource compared, Datasource with, boolean merge) {
    DatasourceCompareDto.Builder dtoBuilder = DatasourceCompareDto.newBuilder() //
        .setCompared(Dtos.asDto(compared)) //
        .setWithDatasource(Dtos.asDto(with));

    for (ValueTable vt : compared.getValueTables()) {
      TableCompareDto tableCompareDto = with.hasValueTable(vt.getName()) //
          ? createTableCompareDto(vt, with.getValueTable(vt.getName()), merge) //
          : createTableCompareDtoWhereSecondTableDoesNotExist(vt);
      dtoBuilder.addTableComparisons(tableCompareDto);
    }

    return dtoBuilder.build();
  }

  private TableCompareDto createTableCompareDto(final ValueTable compared, final ValueTable with, boolean merge) {
    Set<Variable> variablesInCompared = asSet(compared.getVariables());
    Set<Variable> variablesInWith = asSet(with.getVariables());

    Iterable<Variable> newVariables = variablesInCompared.stream().filter(input -> input != null && !with.hasVariable(input.getName())).collect(Collectors.toList());
    Iterable<Variable> missingVariables = variablesInWith.stream().filter(input -> input != null && !compared.hasVariable(input.getName())).collect(Collectors.toList());
    Iterable<Variable> existingVariables = variablesInCompared.stream().filter(input -> input != null && with.hasVariable(input.getName())).collect(Collectors.toList());

    return createTableCompareDto(compared, with, newVariables, missingVariables, existingVariables, merge);
  }

  private TableCompareDto createTableCompareDtoWhereSecondTableDoesNotExist(ValueTable compared) {
    Set<Variable> variablesInCompared = asSet(compared.getVariables());
    Iterable<Variable> newVariables = new LinkedHashSet<>(variablesInCompared);
    Iterable<Variable> missingVariables = new LinkedHashSet<>(INITIAL_CAPACITY);
    Iterable<Variable> existingVariables = new LinkedHashSet<>(INITIAL_CAPACITY);

    return createTableCompareDto(compared, null, newVariables, missingVariables, existingVariables, false);
  }

  private TableCompareDto createTableCompareDto(ValueTable compared, ValueTable with, Iterable<Variable> newVariables,
                                                Iterable<Variable> missingVariables, Iterable<Variable> existingVariables, boolean merge) {
    TableCompareDto.Builder dtoBuilder = TableCompareDto.newBuilder();
    dtoBuilder.setCompared(Dtos.asDto(compared, true, false));

    if (with != null) {
      dtoBuilder.setWithTable(Dtos.asDto(with, true, false));
    }

    Collection<ConflictDto> conflicts = new LinkedHashSet<>(INITIAL_CAPACITY);
    conflicts.addAll(getMissingCsvVariableConflicts(compared));

    conflicts.addAll(getConflicts(compared, with, existingVariables, false));
    conflicts.addAll(getConflicts(compared, with, newVariables, true));
    dtoBuilder.addAllConflicts(conflicts);

    for (Variable v : getUnconflicting(newVariables, conflicts)) {
      dtoBuilder.addNewVariables(Dtos.asDto(v));
    }
    for (Variable v : missingVariables) {
      dtoBuilder.addMissingVariables(Dtos.asDto(v));
    }

    return addTableCompareDtoModifications(dtoBuilder, with, existingVariables, conflicts, merge).build();
  }

  private TableCompareDto.Builder addTableCompareDtoModifications(TableCompareDto.Builder dtoBuilder, ValueTable with,
                                                                  Iterable<Variable> existingVariables, Iterable<ConflictDto> conflicts, boolean merge) {
    Set<Variable> unconflicting = getUnconflicting(existingVariables, conflicts);
    if (with != null) {
      Set<Variable> modifiedVariables = getUnconflictingModified(with, unconflicting, merge);
      for (Variable variable : modifiedVariables) {
        dtoBuilder.addModifiedVariables(Dtos.asDto(variable));
      }
      unconflicting.removeAll(modifiedVariables);
    }
    for (Variable variable : unconflicting) {
      dtoBuilder.addUnmodifiedVariables(Dtos.asDto(variable));
    }

    return dtoBuilder;
  }

  private Collection<ConflictDto> getMissingCsvVariableConflicts(ValueTable compared) {
    Collection<ConflictDto> conflicts = new LinkedHashSet<>(INITIAL_CAPACITY);
    if (compared.getDatasource().getType().equals(CsvDatasource.TYPE)) {
      // support IncrementalView wrapping compared table
      CsvValueTable csvValueTable = (CsvValueTable) (compared instanceof ValueTableWrapper //
          ? ((ValueTableWrapper) compared).getInnermostWrappedValueTable() //
          : compared);
      for (Variable missingVariable : csvValueTable.getMissingVariables()) {
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
    for (Variable variable : variables) {
      if (entityType == null) {
        entityType = variable.getEntityType();
      }
      getVariableConflicts(compared, with, newVariable, conflicts, entityType, variable);
    }
    return conflicts;
  }

  private void getVariableConflicts(ValueTable compared, ValueTable with, boolean newVariable,
                                    Collection<ConflictDto> conflicts, String entityType, Variable variable) {
    if (with == null) {
      // Target (with) will be created
      if (!entityType.equals(variable.getEntityType())) {
        conflicts.add(
            createConflictDto(Dtos.asDto(variable).setIsNewVariable(true).build(), INCOMPATIBLE_ENTITY_TYPE, entityType,
                variable.getEntityType()));
      }
    } else {
      // Target (with) table already exist
      String name = variable.getName();
      Variable variableInCompared = compared.getVariable(name);
      if (!variableInCompared.getEntityType().equals(with.getEntityType())) {
        conflicts.add(
            createConflictDto(Dtos.asDto(variable).setIsNewVariable(newVariable).build(), INCOMPATIBLE_ENTITY_TYPE,
                variableInCompared.getEntityType(), with.getEntityType()));
      }
      try {
        Variable variableInWith = with.getVariable(name);
        if (!variableInCompared.getValueType().equals(variableInWith.getValueType()) && !with.isView()) {
          conflicts.add(
              createConflictDto(Dtos.asDto(variable).setIsNewVariable(newVariable).build(), INCOMPATIBLE_VALUE_TYPE,
                  variableInCompared.getValueType().getName(), variableInWith.getValueType().getName()));
        }
      } catch (NoSuchVariableException variableDoesNotExist) {
        // Case where the variable does not exist in Opal but its destination table already exist.
      }
    }
  }

  private Set<Variable> getUnconflicting(Iterable<Variable> variables, Iterable<ConflictDto> conflicts) {
    Set<Variable> unconflicting = new LinkedHashSet<>(INITIAL_CAPACITY);

    Collection<String> conflicting = new LinkedHashSet<>(INITIAL_CAPACITY);
    for (ConflictDto dto : conflicts) {
      conflicting.add(dto.getVariable().getName());
    }

    for (Variable v : variables) {
      if (!conflicting.contains(v.getName())) {
        unconflicting.add(v);
      }
    }

    return unconflicting;
  }

  private Set<Variable> getUnconflictingModified(ValueTable with, Iterable<Variable> variables, boolean merge) {
    Set<Variable> modified = new LinkedHashSet<>(INITIAL_CAPACITY);

    for (Variable v : variables) {
      Variable withVar = with.getVariable(v.getName());
      if (VariableHelper.isModified(v, withVar)) {
        if (merge) {
          modified.add(mergeVariables(withVar, v));
        } else {
          modified.add(v);
        }
      }
    }

    return modified;
  }

  private Variable mergeVariables(Variable original, Variable update) {
    VariableBean.Builder merge = VariableBean.Builder.sameAs(original).overrideWith(update);
    return merge.build();
  }

  private ConflictDto createConflictDto(VariableDto variableDto, String code, String... args) {
    ConflictDto.Builder dtoBuilder = ConflictDto.newBuilder();
    dtoBuilder.setVariable(variableDto);
    dtoBuilder.setCode(code);
    for (String arg : args) {
      dtoBuilder.addArguments(arg);
    }
    return dtoBuilder.build();
  }

  private <T> Set<T> asSet(Iterable<T> iterable) {
    Set<T> set = new LinkedHashSet<>(INITIAL_CAPACITY);
    for (T elem : iterable) {
      set.add(elem);
    }
    return set;
  }
}

