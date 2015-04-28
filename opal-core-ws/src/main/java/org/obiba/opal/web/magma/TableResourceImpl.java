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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.views.View;
import org.obiba.magma.views.support.AllClause;
import org.obiba.opal.core.service.DataImportService;
import org.obiba.opal.core.service.NoSuchIdentifiersMappingException;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.obiba.magma.Attribute.Builder.newAttribute;

@Component("tableResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class TableResourceImpl extends AbstractValueTableResource implements TableResource {

  private DataImportService dataImportService;

  @Autowired
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private Collection<ValueTableUpdateListener> tableListeners;

  @Autowired
  public void setDataImportService(DataImportService dataImportService) {
    this.dataImportService = dataImportService;
  }

  @Override
  public TableDto get(Request request, UriInfo uriInfo, Boolean counts) {
    String path = uriInfo.getPath(false);
    TableDto.Builder builder = Dtos.asDto(getValueTable(), counts).setLink(path);
    if(getValueTable().isView()) {
      builder.setViewLink(path.replaceFirst("table", "view"));
    }
    return builder.build();
  }

  @Override
  public Response update(TableDto table) {
    ValueTable valueTable = getValueTable();
    if(!valueTable.getEntityType().equals(table.getEntityType())) return Response.status(BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "EntityTypeUpdateNotSupported").build()).build();

    if(valueTable.getName().equals(table.getName())) return Response.ok().build();

    if(!getDatasource().canRenameTable(valueTable.getName())) return Response.status(BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "TableRenamingNotSupported").build()).build();

    if(getDatasource().hasValueTable(table.getName())) return Response.status(BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "TableAlreadyExists").build()).build();

    if(tableListeners != null && !tableListeners.isEmpty()) {
      for(ValueTableUpdateListener listener : tableListeners) {
        listener.onRename(getValueTable(), table.getName());
      }
    }
    getDatasource().renameTable(valueTable.getName(), table.getName());

    return Response.ok().build();
  }

  @Override
  public VariablesResource getVariables() {
    VariablesResource resource = applicationContext.getBean("variablesResource", VariablesResource.class);
    resource.setValueTable(getValueTable());
    resource.setLocales(getLocales());
    return resource;
  }

  @Override
  public Set<VariableEntityDto> getEntities() {
    Iterable<VariableEntity> entities = filterEntities(null, null);
    return ImmutableSet.copyOf(Iterables.transform(entities, new Function<VariableEntity, VariableEntityDto>() {
      @Override
      public VariableEntityDto apply(VariableEntity from) {
        return VariableEntityDto.newBuilder().setIdentifier(from.getIdentifier()).build();
      }
    }));
  }

  @Override
  public ValueSetResource getValueSet(Request request, String identifier, String select, Boolean filterBinary) {
    ValueSetResource resource = applicationContext.getBean(ValueSetResource.class);
    resource.setValueTable(getValueTable());
    resource.setEntity(new VariableEntityBean(getValueTable().getEntityType(), identifier));
    return resource;
  }

  @Override
  public Magma.TimestampsDto getValueSetTimestamps(Request request, String identifier) {
    ValueTable table = getValueTable();
    return Dtos.asDto(table.getValueSetTimestamps(new VariableEntityBean(table.getEntityType(), identifier))).build();
  }

  @Override
  public ValueSetResource getVariableValueSet(Request request, String identifier, String variable,
      Boolean filterBinary) {
    ValueSetResource resource = applicationContext.getBean(ValueSetResource.class);
    resource.setValueTable(getValueTable());
    resource.setVariableValueSource(getValueTable().getVariableValueSource(variable));
    resource.setEntity(new VariableEntityBean(getValueTable().getEntityType(), identifier));
    return resource;
  }

  @Override
  public Response updateValueSet(ValueSetsDto valueSetsDto, String unitName, boolean generateIds,
      boolean createVariables, boolean ignoreUnknownIds) throws IOException, InterruptedException {
    ValueTable vt = getValueTable();
    try {
      if(dataImportService == null) {
        writeValueSets(vt.getDatasource().createWriter(vt.getName(), valueSetsDto.getEntityType()), valueSetsDto);
      } else {
        Datasource ds = new StaticDatasource("import");
        // static writers will add entities and variables while writing values
        writeValueSets(ds.createWriter(vt.getName(), valueSetsDto.getEntityType()), valueSetsDto);
        dataImportService
            .importData(ds.getValueTables(), vt.getDatasource().getName(), generateIds,
                           createVariables, ignoreUnknownIds, null);
      }
    } catch(NoSuchIdentifiersMappingException ex) {
      return Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "NoSuchIdentifiersMapping", unitName).build()).build();
    } catch(RuntimeException ex) {
      return Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DatasourceCopierIOException", ex)).build();
    }
    return Response.ok().build();
  }

  @Override
  public ValueSetsResource getValueSets(Request request) {
    ValueSetsResource resource = applicationContext.getBean(ValueSetsResource.class);
    resource.setValueTable(getValueTable());
    return resource;
  }

  @Override
  public ValueSetsResource getVariableValueSets(Request request, String name) {
    ValueSetsResource resource = applicationContext.getBean(ValueSetsResource.class);
    resource.setValueTable(getValueTable());
    resource.setVariableValueSource(getValueTable().getVariableValueSource(name));
    return resource;
  }

  @Override
  public VariableResource getVariable(Request request, String name) {
    return getVariableResource(getValueTable().getVariableValueSource(name), name);
  }

  @Override
  public VariableResource getTransientVariable(String valueTypeName, Boolean repeatable, String scriptQP,
      List<String> categoriesQP, Boolean self, String scriptFP, List<String> categoriesFP, List<String> missingCategories) {
    return getVariableResource(
        getJavascriptVariableValueSource(valueTypeName, repeatable, scriptQP, categoriesQP, scriptFP, categoriesFP,
            missingCategories, self), null);
  }

  @Override
  public Response compileTransientVariable(String valueTypeName, Boolean repeatable, String scriptQP,
      List<String> categoriesQP, Boolean self, String scriptFP, List<String> categoriesFP) {
    JavascriptVariableValueSource variableValueSource = getJavascriptVariableValueSource(valueTypeName, repeatable,
        scriptQP, categoriesQP, scriptFP, categoriesFP, null, self);
    variableValueSource.validateScript();
    return Response.ok().build();
  }

  @Override
  public ValueSetsResource getTransientVariableValueSets(String valueTypeName, Boolean repeatable, String scriptQP,
      List<String> categoriesQP, Boolean self, String scriptFP, List<String> categoriesFP) {

    ValueSetsResource resource = applicationContext.getBean(ValueSetsResource.class);
    resource.setValueTable(getValueTable());
    resource.setVariableValueSource(
        getJavascriptVariableValueSource(valueTypeName, repeatable, scriptQP, categoriesQP, scriptFP, categoriesFP,
            null, self));
    return resource;
  }

  @Override
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "MethodWithTooManyParameters" })
  public ValueSetResource getTransientVariableValueSet( //
      Request request, //
      String identifier, //
      Boolean filterBinary, //
      String valueTypeName, //
      Boolean repeatable, //
      String scriptQP, //
      List<String> categoriesQP, //
      Boolean self, //
      String scriptFP, //
      List<String> categoriesFP) {

    ValueSetResource resource = applicationContext.getBean(ValueSetResource.class);
    resource.setValueTable(getValueTable());
    resource.setVariableValueSource(
        getJavascriptVariableValueSource(valueTypeName, repeatable, scriptQP, categoriesQP, scriptFP, categoriesFP,
            null, self));
    resource.setEntity(new VariableEntityBean(getValueTable().getEntityType(), identifier));
    return resource;
  }

  @Override
  public CompareResource getTableCompare() {
    CompareResource resource = applicationContext.getBean(CompareResource.class);
    resource.setComparedTable(getValueTable());
    return resource;
  }

  @Override
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  //
  // private methods
  //

  private void writeValueSets(ValueTableWriter tableWriter, ValueSetsDto valueSetsDto) {
    try {
      for(ValueSetsDto.ValueSetDto valueSetDto : valueSetsDto.getValueSetsList()) {
        VariableEntity entity = new VariableEntityBean(valueSetsDto.getEntityType(), valueSetDto.getIdentifier());

        try(ValueSetWriter writer = tableWriter.writeValueSet(entity)) {
          for(int i = 0; i < valueSetsDto.getVariablesCount(); i++) {
            Variable variable = getValueTable().getVariable(valueSetsDto.getVariables(i));
            Value value = Dtos.fromDto(valueSetDto.getValues(i), variable.getValueType(), variable.isRepeatable());
            writer.writeValue(variable, value);
          }
        }
      }
    } finally {
      tableWriter.close();
    }
  }

  private ValueType resolveValueType(String valueTypeName) {
    try {
      return ValueType.Factory.forName(valueTypeName);
    } catch(IllegalArgumentException ex) {
      throw new InvalidRequestException("IllegalParameterValue", "valueType", valueTypeName);
    }
  }

  private VariableResource getVariableResource(VariableValueSource source, String name) {
    VariableResource resource = applicationContext.getBean("variableResource", VariableResource.class);
    resource.setName(name);
    resource.setValueTable(getValueTable());
    resource.setVariableValueSource(source);
    return resource;
  }

  private JavascriptVariableValueSource getJavascriptVariableValueSource(String valueTypeName, Boolean repeatable,
      String scriptQP, List<String> categoriesQP, String scriptFP, List<String> categoriesFP,
      Collection<String> missingCategories, boolean self) {
    String script = scriptQP;
    List<String> categories = categoriesQP;
    if(Strings.isNullOrEmpty(script)) {
      script = scriptFP;
    }
    if(Strings.isNullOrEmpty(script)) {
      script = "null";
    }
    if(categories == null || categories.isEmpty()) {
      categories = categoriesFP;
    }
    Variable transientVariable = buildTransientVariable(resolveValueType(valueTypeName), repeatable, script, categories,
        missingCategories);
    ValueTable transientView = self
        ? getValueTable()
        : new View("transient", new AllClause(), new AllClause(), getValueTable());
    JavascriptVariableValueSource valueSource = new JavascriptVariableValueSource(transientVariable, transientView);
    valueSource.initialise();
    return valueSource;
  }

  private Variable buildTransientVariable(ValueType valueType, boolean repeatable, String script,
      @Nullable Iterable<String> categories, @Nullable Collection<String> missingCategories) {
    Variable.Builder builder = new Variable.Builder("_transient", valueType, getValueTable().getEntityType())
        .extend(JavascriptVariableBuilder.class).setScript(script);
    if(repeatable) {
      builder.repeatable();
    }
    if(categories != null) {
      for(String category : categories) {
        boolean isMissing = missingCategories != null && missingCategories.contains(category);
        builder.addCategory(category, "", isMissing);
      }

    }
    // use attribute to detect transient variables as we may need to set the name of the variable
    builder.addAttribute(
        newAttribute().withNamespace("opal").withName("transient").withValue(BooleanType.get().trueValue()).build());
    return builder.build();
  }

}
