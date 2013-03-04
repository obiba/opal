/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.obiba.opal.web.model.Magma.ValueSetsDto.ValueSetDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

/**
 *
 */
public class ValueSetsResource extends AbstractValueTableResource {

  private static final Logger log = LoggerFactory.getLogger(ValueSetsResource.class);

  @Nullable
  private final VariableValueSource vvs;

  @Nullable
  private final Iterable<VariableEntity> entities;

  public ValueSetsResource(ValueTable valueTable) {
    this(valueTable, null, null);
  }

  public ValueSetsResource(ValueTable valueTable, VariableValueSource vvs) {
    this(valueTable, vvs, null);
  }

  public ValueSetsResource(ValueTable valueTable, @Nullable VariableValueSource vvs,
      @Nullable Iterable<VariableEntity> entities) {
    super(valueTable, new HashSet<Locale>());
    this.vvs = vvs;
    this.entities = entities;
  }

  /**
   * Get a chunk of value sets, optionally filters the variables and/or the entities.
   *
   * @param select script for filtering the variables
   * @param offset
   * @param limit
   * @return
   */
  @GET
  // Required to allow passing parameters in the body
  @POST
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 10)
  public Response getValueSets(@Context UriInfo uriInfo, //
      @QueryParam("select") String select, //
      @QueryParam("offset") @DefaultValue("0") int offset, //
      @QueryParam("limit") @DefaultValue("100") int limit, //
      @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary) {

    // filter entities
    Iterable<VariableEntity> variableEntities = entities == null ? filterEntities(null, offset, limit) : entities;
    ValueSetsDto vs = vvs == null
        ? getValueSetsDto(uriInfo, select, variableEntities, filterBinary)
        : getValueSetsDto(uriInfo, variableEntities, filterBinary);

    return TimestampedResponses.ok(getValueTable(), vs).build();
  }

  private ValueSetsDto getValueSetsDto(final UriInfo uriInfo, String select, @SuppressWarnings(
      "ParameterHidesMemberVariable") Iterable<VariableEntity> entities, final boolean filterBinary) {
    final Iterable<Variable> variables = filterVariables(select, 0, null);

    ValueSetsDto.Builder builder = ValueSetsDto.newBuilder().setEntityType(getValueTable().getEntityType());

    builder.addAllVariables(Iterables.transform(variables, new Function<Variable, String>() {

      @Override
      public String apply(Variable from) {
        return from.getName();
      }

    }));

    ImmutableList.Builder<ValueSetDto> valueSetDtoBuilder = ImmutableList.builder();
    for(ValueSetDto dto : Iterables
        .transform(entities, new VariableEntityValueSetDtoFunction(variables, uriInfo, filterBinary))) {
      valueSetDtoBuilder.add(dto);
    }

    builder.addAllValueSets(valueSetDtoBuilder.build());

    return builder.build();
  }

  private ValueSetsDto getValueSetsDto(final UriInfo uriInfo,
      @SuppressWarnings("ParameterHidesMemberVariable") Iterable<VariableEntity> entities, final boolean filterBinary) {
    final Variable variable = vvs.getVariable();
    ValueSetsDto.Builder builder = ValueSetsDto.newBuilder().setEntityType(variable.getEntityType())
        .addVariables(vvs.getVariable().getName());

    VectorSource vector = vvs.asVectorSource();
    if(vector == null) {
      builder.addAllValueSets(Iterables.transform(entities, new Function<VariableEntity, ValueSetDto>() {
        @Override
        public ValueSetDto apply(VariableEntity fromEntity) {
          ValueSet valueSet = getValueTable().getValueSet(fromEntity);
          Value value = vvs.getValue(valueSet);
          return getValueSetDto(uriInfo, fromEntity, variable, filterBinary, value);
        }
      }));
    } else {
      addValueSetDtosFromVectorSource(uriInfo, entities, variable, filterBinary, vector, builder);
    }

    return builder.build();
  }

  private void addValueSetDtosFromVectorSource(UriInfo uriInfo, Iterable<VariableEntity> entities, Variable variable,
      boolean filterBinary, VectorSource vector, ValueSetsDto.Builder builder) {
    ImmutableSortedSet<VariableEntity> sortedEntities = ImmutableSortedSet.<VariableEntity>naturalOrder()
        .addAll(entities).build();
    Iterable<Value> values = vector.getValues(sortedEntities);

    HashMap<VariableEntity, Value> results = new LinkedHashMap<VariableEntity, Value>();
    Iterator<VariableEntity> entitiesIterator = sortedEntities.iterator();
    for(Value value : values) {
      VariableEntity entity = entitiesIterator.next();
      results.put(entity, value);
    }

    for(VariableEntity entity : entities) {
      builder.addValueSets(getValueSetDto(uriInfo, entity, variable, filterBinary, results.get(entity)));
    }
  }

  private ValueSetDto getValueSetDto(UriInfo uriInfo, VariableEntity fromEntity, Variable variable,
      boolean filterBinary, Value value) {
    String link = uriInfo.getPath().replace("valueSets",
        "valueSet/entity/" + fromEntity.getIdentifier() + "/variable/" + variable.getName() + "/value");
    log.info("getValueSetDto(uri={})", uriInfo.getPath());
    return ValueSetsDto.ValueSetDto.newBuilder().setIdentifier(fromEntity.getIdentifier())
        .addValues(Dtos.asDto(link, value, filterBinary)).build();
  }

  private class VariableEntityValueSetDtoFunction implements Function<VariableEntity, ValueSetDto> {

    private final Iterable<Variable> variables;

    private final UriInfo uriInfo;

    private final boolean filterBinary;

    public VariableEntityValueSetDtoFunction(Iterable<Variable> variables, UriInfo uriInfo, boolean filterBinary) {
      this.variables = variables;
      this.uriInfo = uriInfo;
      this.filterBinary = filterBinary;
    }

    @Override
    public ValueSetDto apply(final VariableEntity fromEntity) {
      final ValueSet valueSet = getValueTable().getValueSet(fromEntity);

      Iterable<ValueSetsDto.ValueDto> valueDtosIter = Iterables
          .transform(variables, new Function<Variable, ValueSetsDto.ValueDto>() {

            @Override
            public ValueSetsDto.ValueDto apply(Variable fromVariable) {
              String link = uriInfo.getPath().replace("valueSets",
                  "valueSet/entity/" + fromEntity.getIdentifier() + "/variable/" + fromVariable.getName() +
                      "/value");
              Value value = getValueTable().getVariableValueSource(fromVariable.getName()).getValue(valueSet);
              return Dtos.asDto(link, value, filterBinary).build();
            }
          });

      // Do not add iterable directly otherwise the values will be fetched as many times it is iterated
      // (i.e. 2 times, see AbstractMessageLite.addAll()).
      ImmutableList.Builder<ValueSetsDto.ValueDto> valueDtos = ImmutableList.builder();
      for(ValueSetsDto.ValueDto dto : valueDtosIter) {
        valueDtos.add(dto);
      }

      return Dtos.asDto(valueSet).addAllValues(valueDtos.build()) //
          .setTimestamps(Dtos.asDto(valueSet.getTimestamps())).build();
    }
  }
}
