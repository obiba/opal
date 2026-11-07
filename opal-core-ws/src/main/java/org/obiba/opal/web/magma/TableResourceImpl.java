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

import org.obiba.magma.*;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.core.cfg.OpalViewManager;
import org.obiba.opal.core.cfg.OpalViewManager.DependentView;
import org.obiba.opal.core.event.ValueTableRenamedEvent;
import org.obiba.opal.core.service.DataImportService;
import org.obiba.opal.core.service.NoSuchIdentifiersMappingException;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Component("tableResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class TableResourceImpl extends AbstractValueTableResource implements TableResource {

  private DataImportService dataImportService;

  private OpalViewManager viewManager;

  @Autowired
  public void setDataImportService(DataImportService dataImportService) {
    this.dataImportService = dataImportService;
  }

  @Autowired
  public void setViewManager(OpalViewManager viewManager) {
    this.viewManager = viewManager;
  }

  protected OpalViewManager getViewManager() {
    return viewManager;
  }

  protected List<DependentView> getDependentViews() {
    return viewManager.getDependentViews(getDatasource().getName(), getValueTable().getName());
  }

  @Override
  public TableDto get(Request request, UriInfo uriInfo, Boolean counts) {
    String path = uriInfo.getPath(false);
    TableDto.Builder builder = Dtos.asDto(getValueTable(), counts).setLink(path);
    if (getValueTable().isView()) {
      builder.setViewLink(path.replaceFirst("/table/", "/view/"));
    }
    List<DependentView> dependents = getDependentViews();
    List<String> visible = TableDependents.visible(dependents);
    builder.addAllDependents(visible);
    if (visible.size() < dependents.size()) builder.setHiddenDependents(dependents.size() - visible.size());
    return builder.build();
  }

  @Override
  public Response update(TableDto table) {
    ValueTable valueTable = getValueTable();
    if (!valueTable.getEntityType().equals(table.getEntityType())) return Response.status(BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "EntityTypeUpdateNotSupported").build()).build();

    if (valueTable.getName().equals(table.getName())) return Response.ok().build();

    if (ReservedNames.isReserved(table.getName())) return Response.status(BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "ReservedTableName").build()).build();

    if (!getDatasource().canRenameTable(valueTable.getName())) return Response.status(BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "TableRenamingNotSupported").build()).build();

    if (getDatasource().hasValueTable(table.getName())) return Response.status(BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "TableAlreadyExists").build()).build();

    List<DependentView> dependents = getDependentViews();
    if (!dependents.isEmpty()) return TableDependents.conflict(dependents);

    getEventBus().post(new ValueTableRenamedEvent(getValueTable(), table.getName()));
    getDatasource().renameTable(valueTable.getName(), table.getName());

    return Response.ok().build();
  }

  @Override
  public VariablesResource getVariables() {
    String beanName = getValueTable().isView() ? "variablesViewResource" : "variablesResource";
    VariablesResource resource = applicationContext.getBean(beanName, VariablesResource.class);
    resource.setValueTable(getValueTable());
    resource.setLocales(getLocales());
    return resource;
  }

  @Override
  public List<VariableEntityDto> getEntities() {
    List<VariableEntity> entities = filterEntities(null, null);
    return entities.stream()
        .map(from -> VariableEntityDto.newBuilder().setIdentifier(from.getIdentifier()).build())
        .collect(Collectors.toList());
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
                                 boolean ignoreUnknownIds) throws IOException, InterruptedException {
    ValueTable vt = getValueTable();
    try {
      if (dataImportService == null) {
        writeValueSets(vt.getDatasource().createWriter(vt.getName(), valueSetsDto.getEntityType()), valueSetsDto);
      } else {
        Datasource ds = new StaticDatasource("import");
        // static writers will add entities and variables while writing values
        writeValueSets(ds.createWriter(vt.getName(), valueSetsDto.getEntityType()), valueSetsDto);
        dataImportService
            .importData(ds.getValueTables(), vt.getDatasource().getName(), unitName, generateIds, ignoreUnknownIds, null);
      }
    } catch (NoSuchIdentifiersMappingException ex) {
      return Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "NoSuchIdentifiersMapping", unitName).build()).build();
    } catch (RuntimeException ex) {
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
      for (ValueSetsDto.ValueSetDto valueSetDto : valueSetsDto.getValueSetsList()) {
        VariableEntity entity = new VariableEntityBean(valueSetsDto.getEntityType(), valueSetDto.getIdentifier());

        try (ValueSetWriter writer = tableWriter.writeValueSet(entity)) {
          for (int i = 0; i < valueSetsDto.getVariablesCount(); i++) {
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

  private VariableResource getVariableResource(VariableValueSource source, String name) {
    VariableResource resource = applicationContext.getBean("variableResource", VariableResource.class);
    resource.setName(name);
    resource.setValueTable(getValueTable());
    resource.setVariableValueSource(source);
    return resource;
  }

}
