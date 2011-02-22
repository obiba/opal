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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BooleanType;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.ValueSetDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Component
@Scope("prototype")
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
  public VariablesResource getVariables() {
    return new VariablesResource(getValueTable());
  }

  /**
   * Get the entities, optionally filtered by a script.
   * @param script script for filtering the entities
   * @return
   */
  @GET
  @Path("/entities")
  public Set<VariableEntityDto> getEntities(@QueryParam("script") String script) {
    Iterable<VariableEntity> entities = filterEntities(getValueTable(), script);

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
  public ValueSetDto getValueSet(@PathParam("identifier") String identifier, @QueryParam("select") String select) {
    VariableEntity entity = new VariableEntityBean(this.getValueTable().getEntityType(), identifier);
    Iterable<Variable> variables = filterVariables(select, 0, null);
    return getValueSet(entity, variables);
  }

  private ValueSetDto getValueSet(VariableEntity entity, Iterable<Variable> variables) {
    ValueSet valueSet = this.getValueTable().getValueSet(entity);
    ValueSetDto.Builder builder = ValueSetDto.newBuilder().setEntity(VariableEntityDto.newBuilder().setIdentifier(entity.getIdentifier()));
    for(Variable variable : variables) {
      Value value = this.getValueTable().getValue(variable, valueSet);
      builder.addVariables(variable.getName()).addValues(Dtos.asDto(value));
    }
    return builder.build();
  }

  /**
   * Get a chunk of value sets, optionally filters the variables and/or the entities.
   * @param select script for filtering the variables
   * @param where script for filtering the entities
   * @param offset
   * @param limit
   * @return
   */
  @GET
  @Path("/valueSets")
  public Collection<ValueSetDto> getValueSets(@QueryParam("select") String select, @QueryParam("where") String where, @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("100") int limit) {
    Iterable<Variable> variables = filterVariables(select, 0, null);

    List<VariableEntity> entities;
    if(where != null) {
      entities = ImmutableList.copyOf(getFilteredEntities(getValueTable(), where));
    } else {
      entities = new ArrayList<VariableEntity>(getValueTable().getVariableEntities());
    }
    int end = Math.min(offset + limit, entities.size());

    ImmutableList.Builder<ValueSetDto> dtos = ImmutableList.builder();
    for(VariableEntity entity : entities.subList(offset, end)) {
      dtos.add(getValueSet(entity, variables));
    }
    return dtos.build();
  }

  @GET
  @Path("/eval")
  public Iterable<ValueDto> eval(@QueryParam("valueType") String valueType, @QueryParam("script") String script, @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("10") int limit) {
    JavascriptValueSource jvs = newJavaScriptValueSource(ValueType.Factory.forName(valueType), script);

    List<VariableEntity> entities = new ArrayList<VariableEntity>(getValueTable().getVariableEntities());
    int end = Math.min(offset + limit, entities.size());
    Iterable<Value> values = jvs.asVectorSource().getValues(new TreeSet<VariableEntity>(entities.subList(offset, end)));
    return Iterables.transform(values, Dtos.valueAsDtoFunc);
  }

  @Path("/variable/{variable}")
  public VariableResource getVariable(@PathParam("variable") String name) {
    return getVariableResource(getValueTable().getVariableValueSource(name));
  }

  @Path("/variable/_transient")
  public VariableResource getTransient(@QueryParam("valueType") @DefaultValue("text") String valueTypeName, @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String script, @QueryParam("category") List<String> categories) {
    Variable transientVariable = buildTransientVariable(resolveValueType(valueTypeName), repeatable, script, categories == null ? ImmutableList.<String> of() : categories);
    JavascriptVariableValueSource jvvs = new JavascriptVariableValueSource(transientVariable, getValueTable());
    jvvs.initialise();
    return getVariableResource(jvvs);
  }

  @Path("/compare")
  public CompareResource getTableCompare() {
    return new CompareResource(getValueTable());
  }

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

  private Iterable<VariableEntity> filterEntities(ValueTable valueTable, String script) {
    if(script == null) {
      return valueTable.getVariableEntities();
    }

    return getFilteredEntities(valueTable, script);
  }

  private Iterable<VariableEntity> getFilteredEntities(ValueTable valueTable, String script) {
    if(script == null) {
      throw new IllegalArgumentException("Entities filter script cannot be null.");
    }

    JavascriptValueSource jvs = newJavaScriptValueSource(BooleanType.get(), script);

    final SortedSet<VariableEntity> entities = new TreeSet<VariableEntity>(valueTable.getVariableEntities());
    final Iterator<Value> values = jvs.asVectorSource().getValues(entities).iterator();

    return Iterables.filter(entities, new Predicate<VariableEntity>() {

      @Override
      public boolean apply(VariableEntity input) {
        return values.next().getValue() == Boolean.TRUE;
      }
    });
  }

  private JavascriptValueSource newJavaScriptValueSource(ValueType valueType, String script) {
    JavascriptValueSource jvs = new JavascriptValueSource(valueType, script) {
      @Override
      protected void enterContext(MagmaContext ctx, Scriptable scope) {
        if(getValueTable() instanceof ValueTableWrapper) {
          ctx.push(ValueTable.class, ((ValueTableWrapper) getValueTable()).getWrappedValueTable());
        } else {
          ctx.push(ValueTable.class, getValueTable());
        }
      }
    };
    jvs.initialise();
    return jvs;
  }

  private Variable buildTransientVariable(ValueType valueType, boolean repeatable, String script, List<String> categories) {
    Variable.Builder builder = new Variable.Builder("transient", valueType, getValueTable().getEntityType()).extend(JavascriptVariableBuilder.class).setScript(script);

    if(repeatable) {
      builder.repeatable();
    }
    builder.addCategories(categories.toArray(new String[] {}));

    return builder.build();
  }

}
