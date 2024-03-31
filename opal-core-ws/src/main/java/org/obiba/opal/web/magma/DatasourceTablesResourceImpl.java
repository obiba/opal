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

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import org.apache.shiro.SecurityUtils;
import org.obiba.magma.*;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.security.MagmaSecurityExtension;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.event.ValueTableAddedEvent;
import org.obiba.opal.core.event.ValueTableDeletedEvent;
import org.obiba.opal.core.security.OpalPermissions;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Opal.AclAction;
import org.obiba.opal.web.security.AuthorizationInterceptor;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.AuthorizeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class DatasourceTablesResourceImpl implements AbstractTablesResource, DatasourceTablesResource {

  private static final Logger log = LoggerFactory.getLogger(DatasourceTablesResourceImpl.class);

  private Datasource datasource;

  private ViewManager viewManager;

  @Autowired
  protected OpalSearchService opalSearchService;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private EventBus eventBus;

  @Override
  public void setDatasource(Datasource datasource) {
    this.datasource = datasource;
  }

  @Autowired
  public void setViewManager(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  @Override
  public List<TableDto> getTables(Request request, boolean counts, @Nullable String entityType, boolean indexedOnly) {
    return getTables(counts, entityType, indexedOnly);
  }

  @Override
  public List<TableDto> getTables(boolean counts, String entityType, boolean indexedOnly) {
    List<Magma.TableDto> tables = Lists.newArrayList();
    if (datasource == null) return tables;
    List<ValueTable> filteredTables = datasource.getValueTables().stream()
        .filter(vt -> entityType == null || vt.getEntityType().equals(entityType))
        .filter(vt -> !indexedOnly || hasUpToDateIndex(vt))
        .collect(Collectors.toList());
    for(ValueTable valueTable : filteredTables) {
      TableDto.Builder builder;
      try {
        builder = Dtos.asDto(valueTable, counts);
      } catch(Exception e) {
        if (counts) {
          log.warn("Error when evaluating table variables/values counts: " + valueTable.getName(), e);
          builder = Dtos.asDto(valueTable, true, false);
        }
        else throw e;
      }
      tables.add(builder.build());
    }
    sortByName(tables);

    return tables;
  }

  @Override
  @GET
  @Path("/excel")
  @Produces("application/vnd.ms-excel")
  @AuthorizeResource
  @AuthenticatedByCookie
  public Response getExcelDictionary(List<String> tables) throws MagmaRuntimeException, IOException {
    if (datasource == null) throw new NoSuchDatasourceException("?");
    String destinationName = datasource.getName() + "-dictionary";
    ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
    Datasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);
    destinationDatasource.initialise();
    try {
      DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();

      if(tables == null || tables.isEmpty()) {
        copier.copy(datasource, destinationDatasource);
      } else {
        for(ValueTable table : datasource.getValueTables()) {
          if(tables.contains(table.getName())) {
            copier.copy(table, destinationDatasource);
          }
        }
      }

    } finally {
      Disposables.silentlyDispose(destinationDatasource);
    }
    return Response.ok(excelOutput.toByteArray(), "application/vnd.ms-excel")
        .header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
  }

  @Override
  @POST
  public Response createTable(final TableDto table) {
    if (datasource == null) throw new NoSuchDatasourceException("?");
    if(MagmaEngine.get().hasExtension(MagmaSecurityExtension.class)) {
      return MagmaEngine.get().getExtension(MagmaSecurityExtension.class).getAuthorizer()
          .silentSudo(() -> createTableInternal(table));
    }

    return createTableInternal(table);
  }

  private Response createTableInternal(TableDto table) {
    if(datasource.hasValueTable(table.getName())) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "TableAlreadyExists").build()).build();
    }
    writeVariablesToTable(table);
    getEventBus().post(new ValueTableAddedEvent(datasource.getName(), table.getName()));
    URI tableUri = UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getTable")
        .build(datasource.getName(), table.getName());
    return Response.created(tableUri)
        .header(AuthorizationInterceptor.ALT_PERMISSIONS, new OpalPermissions(tableUri, AclAction.TABLE_ALL)).build();
  }

  @Override
  @DELETE
  public Response deleteTables(@QueryParam("table") List<String> tables) {
    if (datasource == null) throw new NoSuchDatasourceException("?");
    for(String table : tables) {
      if(datasource.hasValueTable(table) && datasource.canDropTable(table) && hasDropPermission(table)) {
        ValueTable toDrop = datasource.getValueTable(table);
        if(toDrop.isView()) {
          viewManager.removeView(datasource.getName(), table);
        } else {
          datasource.dropTable(table);
        }
        getEventBus().post(new ValueTableDeletedEvent(toDrop));
        subjectProfileService.deleteBookmarks("/datasource/" + datasource.getName() + "/table/" + table);
      }
    }

    return Response.ok().build();
  }

  private boolean hasDropPermission(String table) {
    return SecurityUtils.getSubject().isPermitted("rest:/datasource/" + datasource.getName() + "/table/" + table + ":DELETE");
  }

  private void writeVariablesToTable(TableDto table) {
    try(VariableWriter variableWriter = datasource.createWriter(table.getName(), table.getEntityType())
        .writeVariables()) {
      for(VariableDto dto : table.getVariablesList()) {
        variableWriter.writeVariable(Dtos.fromDto(dto));
      }
    }
  }

  private void sortByName(List<Magma.TableDto> tables) {
    // sort alphabetically
    Collections.sort(tables, Comparator.comparing(TableDto::getName));
  }

  private boolean hasUpToDateIndex(ValueTable table) {
    return opalSearchService.isRunning() && opalSearchService.isEnabled()
        && opalSearchService.getValuesIndexManager().hasIndex(table)
        && opalSearchService.getValuesIndexManager().getIndex(table).isUpToDate();
  }

  private EventBus getEventBus() {
    return eventBus == null ? eventBus = new EventBus() : eventBus;
  }
}
