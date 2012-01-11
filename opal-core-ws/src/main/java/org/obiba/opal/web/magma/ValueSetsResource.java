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

import java.util.HashSet;
import java.util.Locale;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSequence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.web.model.Magma.ValueSetsDto;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 *
 */
public class ValueSetsResource extends AbstractValueTableResource {

  private Variable variable;

  public ValueSetsResource(ValueTable valueTable) {
    this(valueTable, null);
  }

  public ValueSetsResource(ValueTable valueTable, Variable variable) {
    super(valueTable, new HashSet<Locale>());
    this.variable = variable;
  }

  /**
   * Get a chunk of value sets, optionally filters the variables and/or the entities.
   * @param select script for filtering the variables
   * @param offset
   * @param limit
   * @return
   */
  @GET
  public ValueSetsDto getValueSets(@Context final UriInfo uriInfo, @QueryParam("select") String select, @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("100") int limit) {
    // ignore select parameter if value sets are accessed by variable
    final Iterable<Variable> variables = variable == null ? filterVariables(select, 0, null) : ImmutableList.<Variable> builder().add(variable).build();
    // filter entities
    final Iterable<VariableEntity> entities = filterEntities(null, offset, limit);

    ValueSetsDto.Builder valueSets = ValueSetsDto.newBuilder().addAllVariables(Iterables.transform(variables, new Function<Variable, String>() {

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
          public ValueSetsDto.ValueDto apply(Variable from) {
            // TODO use uri builder
            String link;
            if(variable == null) {
              link = uriInfo.getPath().replace("valueSets", "variable/" + from.getName() + "/value/" + fromEntity.getIdentifier());
            } else {
              link = uriInfo.getPath().replace("valueSets", "value/" + fromEntity.getIdentifier());
            }
            Value value = getValueTable().getVariableValueSource(from.getName()).getValue(valueSet);
            ValueSetsDto.ValueDto.Builder valueDto = Dtos.asValueSetsValueDto(link, value);
            if(value.isNull() == false && value.isSequence()) {
              ValueSequence valueSeq = value.asSequence();
              for(int i = 0; i < valueSeq.getSize(); i++) {
                valueDto.addValues(Dtos.asValueSetsValueDto(link + "?pos=" + i, valueSeq.get(i)).build());
              }
            }
            return valueDto.build();
          }
        })).build();
      }
    }));

    return valueSets.build();
  }

}
