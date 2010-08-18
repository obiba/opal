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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.web.model.Magma.ConflictDto;
import org.obiba.opal.web.model.Magma.TableCompareDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;

@Component
public class CompareResource {
  //
  // Constants
  //

  private static final String INCOMPATIBLE_ENTITY_TYPE = "IncompatibleEntityType";

  private static final String INCOMPATIBLE_VALUE_TYPE = "IncompatibleValueType";

  //
  // Instance Variables
  //

  ValueTable compared;

  //
  // Constructors
  //

  public CompareResource() {
  }

  public CompareResource(ValueTable compared) {
    this.compared = compared;
  }

  //
  // Methods
  //

  @GET
  @Path("/{with}")
  public Response compareTable(@PathParam("with") String with) {
    ValueTable withTable = getValueTable(with);

    TableCompareDto dto = createTableCompareDto(compared, withTable);

    return Response.ok().entity(dto).build();
  }

  @VisibleForTesting
  ValueTable getValueTable(String fqTableName) {
    String datasourceName = MagmaEngineTableResolver.valueOf(fqTableName).getDatasourceName();
    String tableName = MagmaEngineTableResolver.valueOf(fqTableName).getTableName();

    System.out.println("datasourceName " + datasourceName);
    System.out.println("tableName " + tableName);

    ValueTable withTable = MagmaEngine.get().getDatasource(datasourceName).getValueTable(tableName);
    return withTable;
  }

  private TableCompareDto createTableCompareDto(ValueTable compared, ValueTable with) {
    Set<Variable> variablesInCompared = asSet(compared.getVariables());
    Set<Variable> variablesInWith = asSet(with.getVariables());

    Set<Variable> newVariables = new HashSet<Variable>(variablesInCompared);
    newVariables.removeAll(variablesInWith);

    Set<Variable> missingVariables = new HashSet<Variable>(variablesInWith);
    missingVariables.removeAll(variablesInCompared);

    Set<Variable> existingVariables = new HashSet<Variable>(variablesInCompared);
    existingVariables.retainAll(variablesInWith);

    return createTableCompareDto(compared, with, newVariables, missingVariables, existingVariables);
  }

  private TableCompareDto createTableCompareDto(ValueTable compared, ValueTable with, Set<Variable> newVariables, Set<Variable> missingVariables, Set<Variable> existingVariables) {
    TableCompareDto.Builder dtoBuilder = TableCompareDto.newBuilder();
    dtoBuilder.setCompared(Dtos.asDto(compared, null));
    dtoBuilder.setWithTable(Dtos.asDto(with, null));

    Set<ConflictDto> conflicts = getConflicts(compared, with, existingVariables);
    dtoBuilder.addAllConflicts(conflicts);

    for(Variable v : newVariables) {
      dtoBuilder.addNewVariables(Dtos.asDto(v));
    }
    for(Variable v : missingVariables) {
      dtoBuilder.addMissingVariables(Dtos.asDto(v));
    }
    for(Variable v : getUnconflicting(existingVariables, conflicts)) {
      dtoBuilder.addExistingVariables(Dtos.asDto(v));
    }

    return dtoBuilder.build();
  }

  private Set<ConflictDto> getConflicts(ValueTable compared, ValueTable with, Set<Variable> variables) {
    Set<ConflictDto> conflicts = new HashSet<ConflictDto>();

    for(Variable v : variables) {
      String name = v.getName();

      Variable variableInCompared = compared.getVariable(name);
      Variable variableInWith = with.getVariable(name);

      if(!variableInCompared.getEntityType().equals(variableInWith.getEntityType())) {
        conflicts.add(createConflictDto(Dtos.asDto(v).build(), INCOMPATIBLE_ENTITY_TYPE, variableInCompared.getEntityType(), variableInWith.getEntityType()));
      } else if(!variableInCompared.getValueType().equals(variableInWith.getValueType())) {
        conflicts.add(createConflictDto(Dtos.asDto(v).build(), INCOMPATIBLE_VALUE_TYPE, variableInCompared.getValueType().getName(), variableInWith.getValueType().getName()));
      }
    }

    return conflicts;
  }

  private Set<Variable> getUnconflicting(Set<Variable> variables, Set<ConflictDto> conflicts) {
    Set<Variable> unconflicting = new HashSet<Variable>();

    Set<String> conflicting = new HashSet<String>();
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
    Set<T> set = new HashSet<T>();

    for(T elem : iterable) {
      set.add(elem);
    }

    return set;
  }
}
