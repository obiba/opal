/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.web.model.Magma;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import static org.obiba.opal.web.magma.Dtos.asDto;

public class VariableEntityValueSetDtoFunction implements Function<ValueSet, Magma.ValueSetsDto.ValueSetDto> {

  private final ValueTable valueTable;

  private final Iterable<Variable> variables;

  private final String uriInfoPath;

  private final boolean filterBinary;

  public VariableEntityValueSetDtoFunction(ValueTable valueTable, Iterable<Variable> variables, String uriInfoPath,
      boolean filterBinary) {
    this.valueTable = valueTable;
    this.variables = variables;
    this.uriInfoPath = uriInfoPath;
    this.filterBinary = filterBinary;
  }

  @Override
  public Magma.ValueSetsDto.ValueSetDto apply(final ValueSet valueSet) {
    ImmutableList.Builder<Magma.ValueSetsDto.ValueDto> valueDtos = ImmutableList.builder();
    Function<Variable, Magma.ValueSetsDto.ValueDto> variableToValueDto = new Function<Variable, Magma.ValueSetsDto.ValueDto>() {
      @Override
      public Magma.ValueSetsDto.ValueDto apply(Variable fromVariable) {
        String link = uriInfoPath.replace("valueSets",
            "valueSet/entity/" + valueSet.getVariableEntity().getIdentifier() + "/variable/" + fromVariable.getName() +
                "/value");
        Value value = valueTable.getVariableValueSource(fromVariable.getName()).getValue(valueSet);
        return asDto(link, value, filterBinary).build();
      }
    };
    for (Variable variable : variables) {
      valueDtos.add(variableToValueDto.apply(variable));
    }
    return asDto(valueSet).addAllValues(valueDtos.build()) //
        .setTimestamps(asDto(valueSet.getTimestamps())).build();
  }
}