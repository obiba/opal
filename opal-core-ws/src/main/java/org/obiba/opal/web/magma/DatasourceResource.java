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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.runtime.security.support.OpalPermissions;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.Schedule;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.AclAction;
import org.obiba.opal.web.model.Opal.LocaleDto;
import org.obiba.opal.web.security.AuthorizationInterceptor;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.Nullable;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Transactional
@Scope("request")
@Path("/datasource/{name}")
public class DatasourceResource {

  @PathParam("name")
  private String name;

  private OpalConfigurationService configService;

  private OpalGeneralConfigService serverService;

  private ViewManager viewManager;

  private IndexManagerConfigurationService indexManagerConfigService;

  private ViewDtos viewDtos;

  private ApplicationContext applicationContext;

  public void setName(String name) {
    this.name = name;
  }

  @Autowired
  public void setConfigService(OpalConfigurationService configService) {
    this.configService = configService;
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
  public Response get(@Context Request request) {
    Datasource ds = getDatasource();
    TimestampedResponses.evaluate(request, ds);
    return TimestampedResponses.ok(ds, Dtos.asDto(ds).build()).build();
  }

  @DELETE
  public Response removeDatasource() {
    ResponseBuilder response;
    if(MagmaEngine.get().hasTransientDatasource(name)) {
      MagmaEngine.get().removeTransientDatasource(name);
      response = Response.ok();
    } else if(MagmaEngine.get().hasDatasource(name)) {
      MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource(name));
      configService.modifyConfiguration(new ConfigModificationTask() {

        @Override
        public void doWithConfig(OpalConfiguration config) {
          Disposables.dispose(config.getMagmaEngineFactory().removeFactory(name));
        }
      });

      viewManager.removeAllViews(name);

      response = Response.ok();
    } else {
      response = Response.status(Status.NOT_FOUND)
          .entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "DatasourceNotFound"));
    }

    return response.build();
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

  public ViewResource getViewResource(ValueTable view) {
    ViewResource resource = applicationContext.getBean(ViewResource.class);
    resource.setLocales(getLocales());
    resource.setValueTable(view);
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
    if(!viewDto.hasName()) return Response.status(BAD_REQUEST).build();

    if(datasourceHasTable(viewDto.getName())) {
      return Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "TableAlreadyExists").build()).build();
    }
    View view = viewDtos.fromDto(viewDto);

    viewManager.addView(getDatasource().getName(), view, comment);

    URI viewUri = UriBuilder.fromUri(uriInfo.getBaseUri().toString()).path(DatasourceResource.class)
        .path(DatasourceResource.class, "getView").build(name, viewDto.getName());

    ValueTable vt = getDatasource().getValueTable(view.getName());
    Schedule schedule = new Schedule();
    schedule.setType(Opal.ScheduleType.NOT_SCHEDULED);
    indexManagerConfigService.update(vt, schedule);

    return Response.created(viewUri)
        .header(AuthorizationInterceptor.ALT_PERMISSIONS, new OpalPermissions(viewUri, AclAction.VIEW_ALL)).build();
  }

  @Path("/view/{viewName}")
  public ViewResource getView(@PathParam("viewName") String viewName) {
    return getViewResource(viewManager.getView(getDatasource().getName(), viewName));
  }

  @GET
  @Path("/locales")
  @NoAuthorization
  public Iterable<LocaleDto> getLocales(@QueryParam("locale") String displayLocale) {
    Collection<LocaleDto> localeDtos = new ArrayList<LocaleDto>();
    for(Locale locale : getLocales()) {
      localeDtos.add(Dtos.asDto(locale, displayLocale != null ? new Locale(displayLocale) : null));
    }
    return localeDtos;
  }

  Datasource getDatasource() {
    return MagmaEngine.get().hasDatasource(name)
        ? MagmaEngine.get().getDatasource(name)
        : MagmaEngine.get().getTransientDatasourceInstance(name);
  }

  private boolean datasourceHasTable(String viewName) {
    return getDatasource().hasValueTable(viewName);
  }

  private Set<Locale> getLocales() {
    // Get locales from server config
    return Sets.newHashSet(serverService.getConfig().getLocales());
  }
}
