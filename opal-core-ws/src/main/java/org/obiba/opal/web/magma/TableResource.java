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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueSetDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class TableResource extends AbstractValueTableResource {

  public TableResource(ValueTable valueTable, Set<Locale> locales) {
    super(valueTable, locales);
  }

  public TableResource(ValueTable valueTable) {
    this(valueTable, Collections.<Locale> emptySet());
  }

  @GET
  public TableDto get(@Context final UriInfo uriInfo) {
    return Dtos.asDto(getValueTable(), null).setLink(uriInfo.getPath()).build();
  }

  @Path("/variables")
  public VariablesResource getVariables(@Context Request request) {
    TimestampedResponses.evaluate(request, getValueTable());
    return new VariablesResource(getValueTable(), getLocales());
  }

  /**
   * Get the entities, optionally filtered by a script.
   * @param script script for filtering the entities
   * @return
   */
  @GET
  @Path("/entities")
  public Set<VariableEntityDto> getEntities(@QueryParam("script") String script) {
    Iterable<VariableEntity> entities = filterEntities(script);

    return ImmutableSet.copyOf(Iterables.transform(entities, new Function<VariableEntity, VariableEntityDto>() {
      @Override
      public VariableEntityDto apply(VariableEntity from) {
        return VariableEntityDto.newBuilder().setIdentifier(from.getIdentifier()).build();
      }
    }));
  }

  /**
   * Get the value set from the given entity identifier, for the variables filtered by the optional 'select' script.
   * @param identifier
   * @param select script for filtering the variables
   * @return
   */
  @GET
  @Path("/valueSet/{identifier}")
  public Response getValueSet(@Context Request request, @Context final UriInfo uriInfo, @PathParam("identifier") String identifier, @QueryParam("select") String select) {
    TimestampedResponses.evaluate(request, getValueTable());
    VariableEntity entity = new VariableEntityBean(this.getValueTable().getEntityType(), identifier);
    Iterable<Variable> variables = filterVariables(select, 0, null);
    ValueSetDto vs = getValueSet(uriInfo, entity, variables);
    return TimestampedResponses.ok(getValueTable(), vs).build();
  }

  private ValueSetDto getValueSet(final UriInfo uriInfo, VariableEntity entity, Iterable<Variable> variables) {
    ValueSet valueSet = this.getValueTable().getValueSet(entity);
    ValueSetDto.Builder builder = ValueSetDto.newBuilder().setEntity(VariableEntityDto.newBuilder().setIdentifier(entity.getIdentifier()));
    for(Variable variable : variables) {
      Value value = this.getValueTable().getValue(variable, valueSet);
      String link = uriInfo.getPath().replace("valueSet", "variable/" + variable.getName() + "/value");
      builder.addVariables(variable.getName()).addValues(Dtos.asDto(link, value));
    }
    return builder.build();
  }

  @Path("/valueSets")
  public ValueSetsResource getValueSets(@Context Request request) {
    TimestampedResponses.evaluate(request, getValueTable());
    return new ValueSetsResource(getValueTable());
  }

  @Path("/variable/{variable}")
  public VariableResource getVariable(@Context Request request, @PathParam("variable") String name) {
    TimestampedResponses.evaluate(request, getValueTable());
    return getVariableResource(getValueTable().getVariableValueSource(name));
  }

  @Path("/variable/_transient")
  public VariableResource getTransient(@QueryParam("valueType") @DefaultValue("text") String valueTypeName, @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP, @QueryParam("category") List<String> categoriesQP, @FormParam("script") String scriptFP, @FormParam("category") List<String> categoriesFP) {
    String script = scriptQP;
    List<String> categories = categoriesQP;
    if(script == null || script.equals("")) {
      script = scriptFP;
    }
    if(script == null || script.equals("")) {
      script = "null";
    }
    if(categories == null || categories.isEmpty()) {
      categories = categoriesFP;
    }
    Variable transientVariable = buildTransientVariable(resolveValueType(valueTypeName), repeatable, script, categories == null ? ImmutableList.<String> of() : categories);
    JavascriptVariableValueSource jvvs = new JavascriptVariableValueSource(transientVariable, getValueTable());
    jvvs.initialise();
    return getVariableResource(jvvs);
  }

  @Path("/compare")
  public CompareResource getTableCompare() {
    return new CompareResource(getValueTable());
  }

  @Path("/locales")
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  // @Path("/dictionary")
  // public TableResource getDictionary() {
  // return new TableResource(new VariableValueTable(getValueTable()), getLocales());
  // }

  //
  // private methods
  //

  private ValueType resolveValueType(String valueTypeName) {
    ValueType valueType = null;
    try {
      valueType = ValueType.Factory.forName(valueTypeName);
    } catch(IllegalArgumentException ex) {
      throw new InvalidRequestException("IllegalParameterValue", "valueType", valueTypeName);
    }
    return valueType;
  }

  private VariableResource getVariableResource(VariableValueSource source) {
    return new VariableResource(this.getValueTable(), source);
  }

  private Variable buildTransientVariable(ValueType valueType, boolean repeatable, String script, List<String> categories) {
    Variable.Builder builder = new Variable.Builder("_transient", valueType, getValueTable().getEntityType()).extend(JavascriptVariableBuilder.class).setScript(script);

    if(repeatable) {
      builder.repeatable();
    }
    builder.addCategories(categories.toArray(new String[] {}));

    return builder.build();
  }

}
