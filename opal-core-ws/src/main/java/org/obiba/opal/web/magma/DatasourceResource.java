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
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.runtime.security.support.OpalPermissions;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.model.Opal.AclAction;
import org.obiba.opal.web.model.Opal.LocaleDto;
import org.obiba.opal.web.security.AuthorizationInterceptor;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@SuppressWarnings("OverlyCoupledClass")
@Component
@Scope("request")
@Path("/datasource/{name}")
public class DatasourceResource {

  @PathParam("name")
  private String name;

  private final OpalConfigurationService configService;

  private final ViewManager viewManager;

  private final ImportService importService;

  private final OpalSearchService opalSearchService;

  private final StatsIndexManager statsIndexManager;

  private final ElasticSearchProvider esProvider;

  private final ViewDtos viewDtos;

  private final Set<ValueTableUpdateListener> tableListeners;

  @Value("${org.obiba.opal.languages}")
  private String localesProperty;

  private Set<Locale> locales;

  @SuppressWarnings("NullableProblems")
  @Autowired
  public DatasourceResource(OpalConfigurationService configService, ImportService importService,
      ViewManager viewManager, OpalSearchService opalSearchService, StatsIndexManager statsIndexManager,
      ElasticSearchProvider esProvider, ViewDtos viewDtos, Set<ValueTableUpdateListener> tableListeners) {

    if(configService == null) throw new IllegalArgumentException("configService cannot be null");
    if(viewManager == null) throw new IllegalArgumentException("viewManager cannot be null");
    if(importService == null) throw new IllegalArgumentException("importService cannot be null");
    if(opalSearchService == null) throw new IllegalArgumentException("opalSearchService cannot be null");
    if(statsIndexManager == null) throw new IllegalArgumentException("statsIndexManager cannot be null");
    if(esProvider == null) throw new IllegalArgumentException("esProvider cannot be null");
    if(viewDtos == null) throw new IllegalArgumentException("viewDtos cannot be null");

    this.configService = configService;
    this.importService = importService;
    this.viewManager = viewManager;
    this.opalSearchService = opalSearchService;
    this.statsIndexManager = statsIndexManager;
    this.esProvider = esProvider;
    this.viewDtos = viewDtos;
    this.tableListeners = tableListeners;
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  public void setLocalesProperty(String localesProperty) {
    this.localesProperty = localesProperty;
  }

  @GET
  // @Cache(isPrivate = true, mustRevalidate = true, maxAge = 10)
  public Magma.DatasourceDto get() {
    return Dtos.asDto(getDatasource()).build();
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
          DatasourceFactory factory = config.getMagmaEngineFactory().removeFactory(name);
          Disposables.dispose(factory);
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
    return new DatasourceTablesResource(getDatasource());
  }

  public TableResource getTableResource(ValueTable table) {
    return getDatasource().canDropTable(table.getName()) //
        ? new DroppableTableResource(table, getLocales(), importService, opalSearchService, statsIndexManager,
        esProvider, tableListeners) //
        : new TableResource(table, getLocales(), importService, opalSearchService, statsIndexManager, esProvider);
  }

  public ViewResource getViewResource(View view) {
    return new ViewResource(viewManager, view, viewDtos, getLocales(), importService, opalSearchService,
        statsIndexManager, esProvider);
  }

  @Path("/compare")
  public CompareResource getTableCompare() {
    return new CompareResource(getDatasource());
  }

  @POST
  @Path("/views")
  public Response createView(ViewDto viewDto, @Context UriInfo uriInfo) {
    if(!viewDto.hasName()) return Response.status(BAD_REQUEST).build();

    if(datasourceHasTable(viewDto.getName())) {
      return Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "TableAlreadyExists").build()).build();
    }
    View view = viewDtos.fromDto(viewDto);

    viewManager.addView(getDatasource().getName(), view);

    URI viewUri = UriBuilder.fromUri(uriInfo.getBaseUri().toString()).path(DatasourceResource.class)
        .path(DatasourceResource.class, "getView").build(name, viewDto.getName());

    return Response.created(viewUri)
        .header(AuthorizationInterceptor.ALT_PERMISSIONS, new OpalPermissions(viewUri, AclAction.VIEW_ALL)).build();
  }

  @Path("/view/{viewName}")
  public ViewResource getView(@PathParam("viewName") String viewName) {
    View view = viewManager.getView(getDatasource().getName(), viewName);
    return getViewResource(view);
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
    if(locales == null) {
      locales = new LinkedHashSet<Locale>();

      String[] localeNames = localesProperty.split(",");
      for(String localeName : localeNames) {
        locales.add(new Locale(localeName.trim()));
      }
    }

    return locales;
  }
}
