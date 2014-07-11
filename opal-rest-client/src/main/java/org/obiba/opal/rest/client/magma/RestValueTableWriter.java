/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.rest.client.magma;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableDto.Builder;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

class RestValueTableWriter implements ValueTableWriter {

  private final RestValueTable restValueTable;

  RestValueTableWriter(RestValueTable restValueTable) {
    this.restValueTable = restValueTable;
  }

  @Override
  public void close() {
  }

  @Override
  public VariableWriter writeVariables() {
    return new VariableWriter() {

      private final List<Variable> variables = Lists.newArrayList();

      @Override
      public void close() {
        URI variablesResource = restValueTable.newReference("variables");
        Iterable<VariableDto> variableDtos = Iterables
            .transform(variables, Functions.compose(new Function<VariableDto.Builder, VariableDto>() {

              @Override
              public VariableDto apply(Builder input) {
                return input.build();
              }
            }, Dtos.asDtoFunc(null)));

        try {
          checkResponse(restValueTable.getOpalClient().post(variablesResource, variableDtos));
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
        restValueTable.refresh();
      }

      @Override
      public void writeVariable(@NotNull Variable variable) {
        variables.add(variable);
      }

      @Override
      public void removeVariable(@NotNull Variable variable) {
        //To change body of implemented methods use File | Settings | File Templates.
      }

    };
  }

  @NotNull
  @Override
  public ValueSetWriter writeValueSet(@NotNull final VariableEntity entity) {

    return new ValueSetWriter() {

      private final ValueSetsDto.Builder valueSetsDtoBuilder = ValueSetsDto.newBuilder()
          .setEntityType(entity.getType());

      private final ValueSetsDto.ValueSetDto.Builder valueSetDtoBuilder = ValueSetsDto.ValueSetDto.newBuilder()
          .setIdentifier(entity.getIdentifier());

      @Override
      public void close() {
        valueSetsDtoBuilder.addValueSets(valueSetDtoBuilder);
        URI valueSetUri = restValueTable.newReference("valueSet");
        try {
          checkResponse(restValueTable.getOpalClient().post(valueSetUri, valueSetsDtoBuilder.build()));
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void remove() {
        URI valueSetUri = restValueTable.newReference("valueSet", entity.getIdentifier());
        try {
          checkResponse(restValueTable.getOpalClient().delete(valueSetUri));
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void writeValue(@NotNull Variable variable, Value value) {
        valueSetsDtoBuilder.addVariables(variable.getName());
        valueSetDtoBuilder.addValues(Dtos.asDto(value));
      }
    };
  }

  private void checkResponse(HttpResponse response) throws IOException {
    EntityUtils.consume(response.getEntity());
    if(response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
      throw new IOException(response.getStatusLine().getReasonPhrase());
    }
  }
}
