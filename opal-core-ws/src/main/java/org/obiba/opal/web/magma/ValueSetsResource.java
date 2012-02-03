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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

/**
 *
 */
public class ValueSetsResource extends AbstractValueTableResource {

  private VariableValueSource vvs;

  public ValueSetsResource(ValueTable valueTable) {
    this(valueTable, null);
  }

  public ValueSetsResource(ValueTable valueTable, VariableValueSource vvs) {
    super(valueTable, new HashSet<Locale>());
    this.vvs = vvs;
  }

  /**
   * Get a chunk of value sets, optionally filters the variables and/or the entities.
   * @param select script for filtering the variables
   * @param offset
   * @param limit
   * @return
   */
  @GET
  @POST
  public Response getValueSets(@Context final UriInfo uriInfo, @QueryParam("select") String select, @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("100") int limit) {
    // filter entities
    final Iterable<VariableEntity> entities = filterEntities(null, offset, limit);

    ValueSetsDto vs;
    if(vvs == null) {
      vs = getValueSetsDto(uriInfo, select, entities);
    } else {
      // ignore select parameter if value sets are accessed by variable value source
      vs = getValueSetsDto(uriInfo, entities);
    }

    return TimestampedResponses.ok(getValueTable(), vs).build();
  }

  private ValueSetsDto getValueSetsDto(final UriInfo uriInfo, final String select, final Iterable<VariableEntity> entities) {
    final Iterable<Variable> variables = filterVariables(select, 0, null);

    return ValueSetsDto.newBuilder().addAllVariables(Iterables.transform(variables, new Function<Variable, String>() {

      @Override
      public String apply(Variable from) {
        return from.getName();
      }

    })).addAllValueSets(Iterables.transform(entities, new Function<VariableEntity, ValueSetsDto.ValueSetDto>() {

      @Override
      public ValueSetsDto.ValueSetDto apply(final VariableEntity fromEntity) {
        final ValueSet valueSet = getValueTable().getValueSet(fromEntity);
        return ValueSetsDto.ValueSetDto.newBuilder().setIdentifier(fromEntity.getIdentifier()).addAllValues(Iterables.transform(variables, new Function<Variable, ValueSetsDto.ValueDto>() {

          @Override
          public ValueSetsDto.ValueDto apply(Variable fromVariable) {
            String link = uriInfo.getPath().replace("valueSets", "variable/" + fromVariable.getName() + "/value/" + fromEntity.getIdentifier());
            Value value = getValueTable().getVariableValueSource(fromVariable.getName()).getValue(valueSet);
            return Dtos.asValueSetsValueDto(link, value).build();
          }
        })).build();
      }
    })).build();
  }

  private ValueSetsDto getValueSetsDto(final UriInfo uriInfo, final Iterable<VariableEntity> entities) {
    ValueSetsDto.Builder builder = ValueSetsDto.newBuilder().addVariables(vvs.getVariable().getName());

    VectorSource vector = vvs.asVectorSource();
    if(vector != null) {
      addValueSetDtosFromVectorSource(uriInfo, entities, vector, builder);
    } else {
      builder.addAllValueSets(Iterables.transform(entities, new Function<VariableEntity, ValueSetsDto.ValueSetDto>() {

        @Override
        public ValueSetsDto.ValueSetDto apply(final VariableEntity fromEntity) {
          final ValueSet valueSet = getValueTable().getValueSet(fromEntity);
          Value value = vvs.getValue(valueSet);
          return getValueSetDto(uriInfo, fromEntity, value);
        }
      }));
    }

    return builder.build();
  }

  private void addValueSetDtosFromVectorSource(UriInfo uriInfo, Iterable<VariableEntity> entities, VectorSource vector, ValueSetsDto.Builder builder) {
    ImmutableSortedSet<VariableEntity> sortedEntities = ImmutableSortedSet.<VariableEntity> naturalOrder().addAll(entities).build();
    Iterable<Value> values = vector.getValues(sortedEntities);

    HashMap<VariableEntity, Value> results = new LinkedHashMap<VariableEntity, Value>();
    Iterator<VariableEntity> entitiesIterator = sortedEntities.iterator();
    for(Value value : values) {
      VariableEntity entity = entitiesIterator.next();
      results.put(entity, value);
    }

    for(VariableEntity entity : entities) {
      builder.addValueSets(getValueSetDto(uriInfo, entity, results.get(entity)));
    }
  }

  private ValueSetDto getValueSetDto(final UriInfo uriInfo, VariableEntity fromEntity, Value value) {
    String link = uriInfo.getPath().replace("valueSets", "value/" + fromEntity.getIdentifier());
    return ValueSetsDto.ValueSetDto.newBuilder().setIdentifier(fromEntity.getIdentifier()).addValues(Dtos.asValueSetsValueDto(link, value)).build();
  }

}
