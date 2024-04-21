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

import com.google.common.collect.ImmutableList;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.obiba.magma.*;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.obiba.opal.web.model.Magma.ValueSetsDto.ValueSetDto;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class ValueSetsResourceImpl extends AbstractValueTableResource implements ValueSetsResource {

  @Nullable
  private VariableValueSource variableValueSource;

  @Override
  public void setVariableValueSource(@Nullable VariableValueSource variableValueSource) {
    this.variableValueSource = variableValueSource;
  }

  @Override
  public Response getValueSets(UriInfo uriInfo, String select, int offset, int limit, Boolean filterBinary) {
    // filter entities
    List<VariableEntity> variableEntities = filterEntities(offset, limit);
    ValueSetsDto vs = variableValueSource == null
        ? getValueSetsDto(uriInfo, select, variableEntities, filterBinary)
        : getValueSetsDto(uriInfo, variableEntities, filterBinary);
    return TimestampedResponses.ok(getValueTable(), vs).build();
  }

  @Override
  public Response drop(@QueryParam("id") List<String> identifiers) {
    ValueTable table = getValueTable();
    if (identifiers == null || identifiers.isEmpty()) {
      table.dropValueSets();
    } else if (table.isView()) {
      throw new IllegalArgumentException("Cannot remove a value set from a view");
    } else {
      ValueTableWriter vtw = getDatasource().createWriter(table.getName(), table.getEntityType());
      for (String id : identifiers) {
        ValueTableWriter.ValueSetWriter vsw = vtw.writeValueSet(new VariableEntityBean(table.getEntityType(), id));
        vsw.remove();
        vsw.close();
      }
      vtw.close();
    }
    return Response.ok().build();
  }

  @Override
  public Response getValueSetsTimestamps(int offset, int limit) {

    // filter entities
    List<VariableEntity> variableEntities = filterEntities(offset, limit);

    ValueSetsDto.Builder builder = ValueSetsDto.newBuilder().setEntityType(getValueTable().getEntityType());
    variableEntities.stream().map(fromEntity -> {
      Timestamps timestamps = getValueTable().getValueSetTimestamps(fromEntity);
      return ValueSetDto.newBuilder().setIdentifier(fromEntity.getIdentifier())
          .setTimestamps(Dtos.asDto(timestamps)).build();
    }).forEach(builder::addValueSets);

    return TimestampedResponses.ok(getValueTable(), builder.build()).build();
  }

  private ValueSetsDto getValueSetsDto(UriInfo uriInfo, String select, List<VariableEntity> variableEntities,
                                       boolean filterBinary) {
    Iterable<Variable> variables = filterVariables(select, 0, null);

    ValueSetsDto.Builder builder = ValueSetsDto.newBuilder().setEntityType(getValueTable().getEntityType());

    builder.addAllVariables(StreamSupport.stream(variables.spliterator(), false)
        .map(Variable::getName).collect(Collectors.toList()));

    ImmutableList.Builder<ValueSetDto> valueSetDtoBuilder = ImmutableList.builder();
    ValueTable valueTable = getValueTable();
    VariableEntityValueSetDtoFunction toDtoFunc = new VariableEntityValueSetDtoFunction(valueTable, variables, uriInfo.getPath(), filterBinary);
    for (ValueSet valueSet : valueTable.getValueSets(variableEntities)) {
      valueSetDtoBuilder.add(toDtoFunc.apply(valueSet));
    }

    builder.addAllValueSets(valueSetDtoBuilder.build());

    return builder.build();
  }

  @SuppressWarnings("ConstantConditions")
  private ValueSetsDto getValueSetsDto(final UriInfo uriInfo, List<VariableEntity> variableEntities,
                                       final boolean filterBinary) {
    final Variable variable = variableValueSource.getVariable();
    ValueSetsDto.Builder builder = ValueSetsDto.newBuilder().setEntityType(variable.getEntityType())
        .addVariables(variable.getName());

    if (variableValueSource.supportVectorSource()) {
      addValueSetDtosFromVectorSource(uriInfo, variableEntities, variable, filterBinary,
          variableValueSource.asVectorSource(), builder);
    } else {
      for (ValueSet valueSet : getValueTable().getValueSets(variableEntities)) {
        Value value = variableValueSource.getValue(valueSet);
        builder.addValueSets(getValueSetDto(uriInfo, valueSet.getVariableEntity(), variable, filterBinary, value));
      }
    }

    return builder.build();
  }

  private void addValueSetDtosFromVectorSource(UriInfo uriInfo, List<VariableEntity> variableEntities,
                                               Variable variable, boolean filterBinary, VectorSource vector, ValueSetsDto.Builder builder) {
    Iterable<Value> values = vector.getValues(variableEntities);

    Map<VariableEntity, Value> results = new LinkedHashMap<>();
    Iterator<VariableEntity> entitiesIterator = variableEntities.iterator();
    for (Value value : values) {
      VariableEntity entity = entitiesIterator.next();
      results.put(entity, value);
    }

    for (VariableEntity entity : variableEntities) {
      builder.addValueSets(getValueSetDto(uriInfo, entity, variable, filterBinary, results.get(entity)));
    }
  }

  private ValueSetDto getValueSetDto(UriInfo uriInfo, VariableEntity fromEntity, Variable variable,
                                     boolean filterBinary, Value value) {
    String link = uriInfo.getPath().replace("valueSets",
        "valueSet/entity/" + fromEntity.getIdentifier() + "/variable/" + variable.getName() + "/value");
    return ValueSetsDto.ValueSetDto.newBuilder().setIdentifier(fromEntity.getIdentifier())
        .addValues(Dtos.asDto(link, value, filterBinary)).build();
  }

}
