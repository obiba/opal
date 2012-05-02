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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import org.obiba.opal.web.model.Magma.ConflictDto;
import org.obiba.opal.web.model.Magma.DatasourceCompareDto;
import org.obiba.opal.web.model.Magma.TableCompareDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

@NoAuthorization
public class CompareResource {
  //
  // Constants
  //
  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(CompareResource.class);

  private static final String INCOMPATIBLE_ENTITY_TYPE = "IncompatibleEntityType";

  private static final String INCOMPATIBLE_VALUE_TYPE = "IncompatibleValueType";

  private static final String CSV_VARIABLE_MISSING = "CsvVariableMissing";

  //
  // Instance Variables
  //

  Datasource comparedDatasource;

  ValueTable comparedTable;

  //
  // Constructors
  //

  public CompareResource() {
  }

  public CompareResource(Datasource comparedDatasource) {
    this.comparedDatasource = comparedDatasource;
  }

  public CompareResource(ValueTable comparedTable) {
    this.comparedTable = comparedTable;
  }

  //
  // Methods
  //

  @GET
  @Path("/{with}")
  public Response compare(@PathParam("with") String with) {
    if(comparedDatasource != null) {
      Datasource withDatasource = getDatasource(with);
      DatasourceCompareDto dto = createDatasourceCompareDto(comparedDatasource, withDatasource);
      return Response.ok().entity(dto).build();
    } else if(comparedTable != null) {
      ValueTable withTable = getValueTable(with);
      TableCompareDto dto = createTableCompareDto(comparedTable, withTable);
      return Response.ok().entity(dto).build();
    } else {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @VisibleForTesting
  Datasource getDatasource(String datasourceName) {
    return MagmaEngine.get().getDatasource(datasourceName);
  }

  @VisibleForTesting
  ValueTable getValueTable(String fqTableName) {
    String datasourceName = MagmaEngineTableResolver.valueOf(fqTableName).getDatasourceName();
    String tableName = MagmaEngineTableResolver.valueOf(fqTableName).getTableName();
    ValueTable withTable = MagmaEngine.get().getDatasource(datasourceName).getValueTable(tableName);

    return withTable;
  }

  private DatasourceCompareDto createDatasourceCompareDto(Datasource compared, Datasource with) {
    DatasourceCompareDto.Builder dtoBuilder = DatasourceCompareDto.newBuilder();
    dtoBuilder.setCompared(Dtos.asDto(compared));
    dtoBuilder.setWithDatasource(Dtos.asDto(with));

    for(ValueTable vt : compared.getValueTables()) {
      TableCompareDto tableCompareDto = null;
      if(with.hasValueTable(vt.getName())) {
        tableCompareDto = createTableCompareDto(vt, with.getValueTable(vt.getName()));
      } else {
        tableCompareDto = createTableCompareDtoWhereSecondTableDoesNotExist(vt);
      }
      dtoBuilder.addTableComparisons(tableCompareDto);
    }

    return dtoBuilder.build();
  }

  private TableCompareDto createTableCompareDto(ValueTable compared, ValueTable with) {
    Set<Variable> variablesInCompared = asSet(compared.getVariables());
    Set<Variable> variablesInWith = asSet(with.getVariables());

    Set<Variable> newVariables = new LinkedHashSet<Variable>(variablesInCompared);
    newVariables.removeAll(variablesInWith);

    Set<Variable> missingVariables = new LinkedHashSet<Variable>(variablesInWith);
    missingVariables.removeAll(variablesInCompared);

    Set<Variable> existingVariables = new LinkedHashSet<Variable>(variablesInCompared);
    existingVariables.retainAll(variablesInWith);

    return createTableCompareDto(compared, with, newVariables, missingVariables, existingVariables);
  }

  private TableCompareDto createTableCompareDtoWhereSecondTableDoesNotExist(ValueTable compared) {
    Set<Variable> variablesInCompared = asSet(compared.getVariables());
    Set<Variable> newVariables = new LinkedHashSet<Variable>(variablesInCompared);
    Set<Variable> missingVariables = new LinkedHashSet<Variable>(5000);
    Set<Variable> existingVariables = new LinkedHashSet<Variable>(5000);

    return createTableCompareDto(compared, null, newVariables, missingVariables, existingVariables);
  }

  private TableCompareDto createTableCompareDto(ValueTable compared, ValueTable with, Set<Variable> newVariables, Set<Variable> missingVariables, Set<Variable> existingVariables) {
    TableCompareDto.Builder dtoBuilder = TableCompareDto.newBuilder();
    dtoBuilder.setCompared(Dtos.asDto(compared));

    if(with != null) {
      dtoBuilder.setWithTable(Dtos.asDto(with));
    }

    Set<ConflictDto> conflicts = new LinkedHashSet<ConflictDto>(5000);
    conflicts.addAll(getMissingCsvVariableConficts(compared));

    conflicts.addAll(getConflicts(compared, with, existingVariables, false));
    conflicts.addAll(getConflicts(compared, with, newVariables, true));
    dtoBuilder.addAllConflicts(conflicts);

    for(Variable v : getUnconflicting(newVariables, conflicts)) {
      dtoBuilder.addNewVariables(Dtos.asDto(v));
    }
    for(Variable v : missingVariables) {
      dtoBuilder.addMissingVariables(Dtos.asDto(v));
    }

    return addTableCompareDtoModifications(dtoBuilder, compared, with, existingVariables, conflicts).build();
  }

  private TableCompareDto.Builder addTableCompareDtoModifications(TableCompareDto.Builder dtoBuilder, ValueTable compared, ValueTable with, Set<Variable> existingVariables, Set<ConflictDto> conflicts) {
    Set<Variable> unconflictingExistingVariables = getUnconflicting(existingVariables, conflicts);
    Set<Variable> unmodifiedVariables = unconflictingExistingVariables;
    if(with != null) {
      Set<Variable> modifiedVariables = getUnconflictingModified(compared, with, unconflictingExistingVariables);
      for(Variable v : modifiedVariables) {
        dtoBuilder.addModifiedVariables(Dtos.asDto(v));
      }
      unmodifiedVariables.removeAll(modifiedVariables);
    }
    for(Variable v : unmodifiedVariables) {
      dtoBuilder.addUnmodifiedVariables(Dtos.asDto(v));
    }

    return dtoBuilder;
  }

  private Set<ConflictDto> getMissingCsvVariableConficts(ValueTable compared) {
    Set<ConflictDto> conflicts = new LinkedHashSet<ConflictDto>(5000);
    if(compared.getDatasource().getType().equals(CsvDatasource.TYPE)) {
      for(Variable missingVariable : ((CsvValueTable) compared).getMissingVariables()) {
        conflicts.add(createConflictDto(Dtos.asDto(missingVariable).setIsNewVariable(true).build(), CSV_VARIABLE_MISSING));
      }
    }
    return conflicts;
  }

  private Set<ConflictDto> getConflicts(ValueTable compared, ValueTable with, Set<Variable> variables, boolean newVariable) {
    Set<ConflictDto> conflicts = new LinkedHashSet<ConflictDto>(5000);

    String entityType = null;
    for(Variable v : variables) {

      if(entityType == null) {
        entityType = v.getEntityType();
      }

      String name = v.getName();

      // Target (with) table already exist
      if(with != null) {
        Variable variableInCompared = compared.getVariable(name);
        if(variableInCompared.getEntityType().equals(with.getEntityType()) == false) {
          conflicts.add(createConflictDto(Dtos.asDto(v).setIsNewVariable(newVariable).build(), INCOMPATIBLE_ENTITY_TYPE, variableInCompared.getEntityType(), with.getEntityType()));
        }

        try {
          Variable variableInWith = with.getVariable(name);
          if(variableInCompared.getValueType().equals(variableInWith.getValueType()) == false && with.isView() == false) {
            conflicts.add(createConflictDto(Dtos.asDto(v).setIsNewVariable(newVariable).build(), INCOMPATIBLE_VALUE_TYPE, variableInCompared.getValueType().getName(), variableInWith.getValueType().getName()));
          }
        } catch(NoSuchVariableException variableDoesNotExist) {
          // Case where the variable does not exist in Opal but its destination table already exist.
        }

        // Target (with) will be created
      } else {
        if(entityType.equals(v.getEntityType()) == false) {
          conflicts.add(createConflictDto(Dtos.asDto(v).setIsNewVariable(true).build(), INCOMPATIBLE_ENTITY_TYPE, entityType, v.getEntityType()));
        }
      }
    }

    return conflicts;
  }

  private Set<Variable> getUnconflicting(Set<Variable> variables, Set<ConflictDto> conflicts) {
    Set<Variable> unconflicting = new LinkedHashSet<Variable>(5000);

    Set<String> conflicting = new LinkedHashSet<String>(5000);
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

  private Set<Variable> getUnconflictingModified(ValueTable compared, ValueTable with, Set<Variable> variables) {
    Set<Variable> modified = new LinkedHashSet<Variable>(5000);

    for(Variable v : variables) {
      Variable withVar = with.getVariable(v.getName());
      if(isModified(v, withVar)) {
        modified.add(v);
      }
    }

    return modified;
  }

  private boolean isModified(Variable compared, Variable with) {
    if(isModified(compared.getMimeType(), with.getMimeType())) return true;
    if(isModified(compared.getOccurrenceGroup(), with.getOccurrenceGroup())) return true;
    if(isModified(compared.getReferencedEntityType(), with.getReferencedEntityType())) return true;
    if(isModified(compared.getUnit(), with.getUnit())) return true;
    if(isModified(compared.getCategories(), with.getCategories())) return true;
    if(isModified(compared.getAttributes(), with.getAttributes())) return true;

    return false;
  }

  private boolean isModified(String compared, String with) {
    if(compared == null && with == null) return false;
    if((compared == null || compared.isEmpty()) && (with == null || with.isEmpty())) return false;
    if(compared != null && compared.equals(with)) return false;
    return true;
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private boolean isModified(Set<Category> compared, Set<Category> with) {
    if(compared == null && with == null) return false;
    if((compared == null || compared.size() == 0) && (with == null || with.size() == 0)) return false;
    if((compared == null && with != null) || (compared != null && with == null)) return true;
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
        if(found == false) return true;
      }
    }

    return false;
  }

  private boolean isModified(Category compared, Category with) {
    if(compared.isMissing() != with.isMissing()) return true;
    if(isModified(compared.getAttributes(), with.getAttributes())) return true;
    return false;
  }

  private boolean isModified(List<Attribute> compared, List<Attribute> with) {
    if(compared == null && with == null) return false;
    if((compared == null || compared.size() == 0) && (with == null || with.size() == 0)) return false;
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
      if(found == false) return true;
    }

    return false;
  }

  private boolean isModified(Value compared, Value with) {
    if(compared == null && with == null) return false;
    String comparedStr = compared == null ? null : compared.toString();
    String withStr = with == null ? null : with.toString();

    return isModified(comparedStr, withStr);
  }

  private boolean isSameAttribute(Attribute compared, Attribute with) {
    if(compared.getName().equals(with.getName()) == false) return false;
    if((compared.getLocale() == null || compared.getLocale().toString().isEmpty()) && (with.getLocale() == null || with.getLocale().toString().isEmpty())) return true;
    if(compared.getLocale() != null) {
      return compared.getLocale().equals(with.getLocale());
    } else if(with.getLocale() != null) {
      return with.getLocale().equals(compared.getLocale());
    }

    return false;
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
    Set<T> set = new LinkedHashSet<T>(5000);

    for(T elem : iterable) {
      set.add(elem);
    }

    return set;
  }
}
