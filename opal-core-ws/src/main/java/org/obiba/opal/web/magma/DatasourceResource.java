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

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.googlecode.protobuf.format.JsonFormat;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueView;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.MagmaSecurityExtension;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.event.DatasourceDeletedEvent;
import org.obiba.opal.core.event.ValueTableAddedEvent;
import org.obiba.opal.core.security.OpalPermissions;
import org.obiba.opal.core.service.NoSuchResourceReferenceException;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.SQLService;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.Schedule;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.AclAction;
import org.obiba.opal.web.model.Opal.LocaleDto;
import org.obiba.opal.web.security.AuthorizationInterceptor;
import org.obiba.opal.web.support.InvalidRequestException;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.core.Response.Status;
import java.io.BufferedOutputStream;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Transactional
@Scope("request")
@Path("/datasource/{name}")
public class DatasourceResource {

  private final static Authorizer authorizer = new ShiroAuthorizer();

  @PathParam("name")
  private String name;

  private OpalGeneralConfigService serverService;

  private ViewManager viewManager;

  private IndexManagerConfigurationService indexManagerConfigService;

  private ViewDtos viewDtos;

  private ApplicationContext applicationContext;

  @Autowired
  private EventBus eventBus;

  @Autowired
  private SQLService sqlService;

  public void setName(String name) {
    this.name = name;
  }

  @Autowired
  public void setIndexManagerConfigService(IndexManagerConfigurationService indexManagerConfigService) {
    this.indexManagerConfigService = indexManagerConfigService;
  }

  @Autowired
  public void setServerService(OpalGeneralConfigService serverService) {
    this.serverService = serverService;
  }

  @Autowired
  public void setViewDtos(ViewDtos viewDtos) {
    this.viewDtos = viewDtos;
  }

  @Autowired
  public void setViewManager(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @GET
  public Magma.DatasourceDto get(@Context Request request) {
    return Dtos.asDto(getDatasource()).build();
  }

  @DELETE
  public Response removeDatasource() {
    Datasource ds;
    if (MagmaEngine.get().hasTransientDatasource(name)) {
      ds = MagmaEngine.get().getTransientDatasourceInstance(name);
      MagmaEngine.get().removeTransientDatasource(name);
      viewManager.unregisterDatasource(name);
    } else if (MagmaEngine.get().hasDatasource(name)) {
      throw new InvalidRequestException("DatasourceNotFound");
    } else {
      return Response.status(Status.NOT_FOUND)
          .entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "DatasourceNotFound")).build();
    }

    getEventBus().post(new DatasourceDeletedEvent(ds));

    return Response.ok().build();
  }

  @Path("/table/{table}")
  public TableResource getTable(@PathParam("table") String table) {
    return getTableResource(getDatasource().getValueTable(table));
  }

  @Path("/tables")
  public DatasourceTablesResource getTables() {
    DatasourceTablesResource resource = applicationContext.getBean(DatasourceTablesResource.class);
    resource.setDatasource(getDatasource());
    return resource;
  }

  public TableResource getTableResource(ValueTable table) {
    TableResource resource = getDatasource().canDropTable(table.getName())
        ? applicationContext.getBean("droppableTableResource", DroppableTableResource.class)
        : applicationContext.getBean("tableResource", TableResource.class);
    resource.setValueTable(table);
    resource.setLocales(getLocales());
    return resource;
  }

  @Path("/compare")
  public CompareResource getTableCompare() {
    CompareResource resource = applicationContext.getBean(CompareResource.class);
    resource.setComparedDatasource(getDatasource());
    return resource;
  }

  @POST
  @Path("/views")
  public Response createView(ViewDto viewDto, @Context UriInfo uriInfo,
                             @Nullable @QueryParam("comment") String comment) {

    if (!viewDto.hasName()) {
      return Response.status(BAD_REQUEST).build();
    }

    if (datasourceHasTable(viewDto.getName())) {
      return Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "TableAlreadyExists").build()).build();
    }

    // check the permissions and if table/resource exists for the user
    AclAction action = getAction(viewDto);

    ValueView view = viewDtos.fromDto(viewDto);
    viewManager.addView(getDatasource().getName(), view, comment, null);
    scheduleViewIndexation(view);
    getEventBus().post(new ValueTableAddedEvent(getDatasource().getName(), viewDto.getName()));

    URI viewUri = UriBuilder.fromUri(uriInfo.getBaseUri().toString()).path(DatasourceResource.class)
        .path(DatasourceResource.class, "getView").build(name, viewDto.getName());
    return Response.created(viewUri)
        .header(AuthorizationInterceptor.ALT_PERMISSIONS, new OpalPermissions(viewUri, action)).build();
  }

  @GET
  @Path("/views")
  public Response backupViews(@QueryParam("views") List<String> viewNames) {

    List<ViewDto> views = getDatasource().getValueTables().stream()
        .filter(valueTable -> valueTable.isView() && (
            (viewNames != null && viewNames.contains(valueTable.getName())) || (viewNames == null || viewNames.size() == 0)))
        .map(valueTable -> viewDtos.asDto(viewManager.getView(valueTable.getDatasource().getName(), valueTable.getName()))
            .toBuilder().clearStatus().build())
        .collect(Collectors.toList());

    StreamingOutput outputStream = stream -> {
      try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(stream))) {
        for (ViewDto viewDto : views) {
          zipOutputStream.putNextEntry(new ZipEntry(viewDto.getName() + ".json"));
          zipOutputStream.write(JsonFormat.printToString(viewDto).getBytes());
          zipOutputStream.closeEntry();
        }
      }
    };

    return Response
        .ok(outputStream, MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "inline; filename=\"backup-" + name + "-" + new Date().getTime() + ".zip\"")
        .build();
  }

  @Path("/view/{viewName}")
  public ViewResource getView(@PathParam("viewName") String viewName) {
    return getViewResource(viewManager.getView(getDatasource().getName(), viewName));
  }

  @GET
  @Path("/locales")
  @NoAuthorization
  public Iterable<LocaleDto> getLocales(@QueryParam("locale") String displayLocale) {
    Collection<LocaleDto> localeDtos = new ArrayList<>();
    for (Locale locale : getLocales()) {
      localeDtos.add(Dtos.asDto(locale, displayLocale == null ? null : new Locale(displayLocale)));
    }
    return localeDtos;
  }

  @POST
  @Path("/_sql")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.APPLICATION_JSON)
  public Response executeSQLToJSON(String query, @QueryParam("id") @DefaultValue(SQLService.DEFAULT_ID_COLUMN) String idName) {
    final File output = sqlService.execute(name, query, idName, SQLService.Output.JSON);
    StreamingOutput stream = os -> {
      Files.copy(output.toPath(), os);
      output.delete();
    };

    return Response.ok(stream, MediaType.APPLICATION_JSON_TYPE)
        .header("Content-Disposition", "attachment; filename=\"" + output.getName() + "\"").build();
  }

  @POST
  @Path("/_sql")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces("text/csv")
  public Response executeSQLToCSV(@FormParam("query") String query, @FormParam("id") @DefaultValue(SQLService.DEFAULT_ID_COLUMN) String idName) {
    final File output = sqlService.execute(name, query, idName, SQLService.Output.CSV);
    StreamingOutput stream = os -> {
      Files.copy(output.toPath(), os);
      output.delete();
    };

    return Response.ok(stream, "text/csv")
        .header("Content-Disposition", "attachment; filename=\"" + output.getName() + "\"").build();
  }

  @POST
  @Path("/_rsql")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces("application/x-rdata")
  public Response executeSQLToRDS(@FormParam("query") String query, @FormParam("id") @DefaultValue(SQLService.DEFAULT_ID_COLUMN) String idName) {
    final File output = sqlService.execute(name, query, idName, SQLService.Output.RDS);
    StreamingOutput stream = os -> {
      Files.copy(output.toPath(), os);
      output.delete();
    };

    return Response.ok(stream, "application/x-rdata")
        .header("Content-Disposition", "attachment; filename=\"" + output.getName() + "\"").build();
  }

  Datasource getDatasource() {
    return MagmaEngine.get().hasDatasource(name)
        ? MagmaEngine.get().getDatasource(name)
        : MagmaEngine.get().getTransientDatasourceInstance(name);
  }

  //
  // Private methods
  //


  private AclAction getAction(ViewDto viewDto) {
    return viewDto.hasExtension(Magma.ResourceViewDto.view) ? getResourceAction(viewDto) : getTableAction(viewDto);
  }

  private AclAction getTableAction(ViewDto viewDto) {
    AclAction action = AclAction.TABLE_ALL;
    if (!MagmaEngine.get().hasExtension(MagmaSecurityExtension.class)) return action;

    for (String tableName : viewDto.getFromList()) {
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      ValueTable table = resolver.resolveTable();
      if (!MagmaEngine.get().getExtension(MagmaSecurityExtension.class).getAuthorizer().isPermitted(
          "rest:/datasource/" + table.getDatasource().getName() + "/table/" + table.getName() + "/valueSet:GET")) {
        action = AclAction.TABLE_EDIT;
        break;
      }
    }
    return action;
  }

  private AclAction getResourceAction(ViewDto viewDto) {
    AclAction action = AclAction.TABLE_ALL;

    for (String resourceName : viewDto.getFromList()) {
      // reuse project's table naming for the resource...
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(resourceName);
      String projectName = resolver.getDatasourceName();
      String resName = resolver.getTableName();
      if (!canViewResourceReference(projectName, resName))
        throw new NoSuchResourceReferenceException(projectName, resName);
      if (!canEditResourceReference(projectName, resName))
        action = AclAction.TABLE_READ;
    }
    return action;
  }

  private void scheduleViewIndexation(ValueTable view) {
    Schedule schedule = new Schedule();
    schedule.setType(Opal.ScheduleType.NOT_SCHEDULED);
    indexManagerConfigService.update(view, schedule);
  }

  private ViewResource getViewResource(ValueView view) {
    ViewResource resource = applicationContext.getBean(ViewResource.class);
    resource.setLocales(getLocales());
    resource.setValueTable(view);
    return resource;
  }

  private boolean datasourceHasTable(String viewName) {
    return getDatasource().hasValueTable(viewName);
  }

  private Set<Locale> getLocales() {
    // Get locales from server config
    return Sets.newHashSet(serverService.getConfig().getLocales());
  }

  private EventBus getEventBus() {
    return eventBus == null ? eventBus = new EventBus() : eventBus;
  }

  private boolean canViewResourceReference(String project, String name) {
    return authorizer.isPermitted("rest:/project/" + project + "/resource/" + name + ":GET");
  }

  private boolean canEditResourceReference(String project, String name) {
    return authorizer.isPermitted("rest:/project/" + project + "/resource/" + name + ":PUT");
  }

}
