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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.views.View;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.model.Opal.LocaleDto;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/datasource/{name}")
public class DatasourceResource {

  @PathParam("name")
  private String name;

  private final OpalRuntime opalRuntime;

  private final ViewDtos viewDtos;

  private Datasource transientDatasourceInstance;

  @Autowired
  @Value("${org.obiba.opal.languages}")
  private String localesProperty;

  private Set<Locale> locales;

  @Autowired
  public DatasourceResource(OpalRuntime opalRuntime, ViewDtos viewDtos) {
    super();
    if(opalRuntime == null) throw new IllegalArgumentException("opalRuntime cannot be null");
    if(viewDtos == null) throw new IllegalArgumentException("viewDtos cannot be null");

    this.opalRuntime = opalRuntime;
    this.viewDtos = viewDtos;
  }

  // Used for testing
  DatasourceResource(String name) {
    this(null, null, name);
  }

  // Used for testing
  DatasourceResource(OpalRuntime opalRuntime, ViewDtos viewDtos, String name) {
    this.opalRuntime = opalRuntime;
    this.viewDtos = viewDtos;
    this.name = name;
  }

  @PreDestroy
  public void destroy() {
    if(transientDatasourceInstance != null) {
      Disposables.silentlyDispose(transientDatasourceInstance);
      transientDatasourceInstance = null;
    }
  }

  public void setLocalesProperty(String localesProperty) {
    this.localesProperty = localesProperty;
  }

  @GET
  public Magma.DatasourceDto get() {
    Datasource ds = getDatasource();

    return Dtos.asDto(ds).build();
  }

  @DELETE
  public Response removeDatasource() {
    ResponseBuilder response = null;
    if(MagmaEngine.get().hasTransientDatasource(name)) {
      MagmaEngine.get().removeTransientDatasource(name);
      response = Response.ok();
    } else if(MagmaEngine.get().hasDatasource(name)) {
      MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource(name));
      opalRuntime.getOpalConfiguration().getMagmaEngineFactory().removeFactory(name);
      opalRuntime.writeOpalConfiguration();
      response = Response.ok();
    } else {
      response = Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "DatasourceNotFound"));
    }

    return response.build();
  }

  @GET
  @Path("/variables/excel")
  @Produces("application/vnd.ms-excel")
  @NotAuthenticated
  public Response getExcelDictionary() throws MagmaRuntimeException, IOException {
    String destinationName = name + "-dictionary";
    ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
    ExcelDatasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);

    destinationDatasource.initialise();
    try {
      DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();
      copier.copy(getDatasource(), destinationDatasource);
    } finally {
      Disposables.silentlyDispose(destinationDatasource);
    }
    return Response.ok(excelOutput.toByteArray(), "application/vnd.ms-excel").header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
  }

  @Path("/table/{table}")
  public TableResource getTable(@PathParam("table") String table) {
    return getTableResource(getDatasource().getValueTable(table));
  }

  @Path("/tables")
  public TablesResource getTables() {
    return new TablesResource(getDatasource());
  }

  public TableResource getTableResource(ValueTable table) {
    return new TableResource(table, getLocales());
  }

  public ViewResource getViewResource(View view) {
    return new ViewResource(opalRuntime, view, viewDtos, getLocales());
  }

  @Path("/compare")
  public CompareResource getTableCompare() {
    return new CompareResource(getDatasource());
  }

  @POST
  @Path("/views")
  public Response createView(ViewDto viewDto, @Context UriInfo uriInfo) {
    if(!viewDto.hasName()) return Response.status(Status.BAD_REQUEST).build();

    if(datasourceHasTable(viewDto.getName())) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "TableAlreadyExists").build()).build();
    }
    opalRuntime.getViewManager().addView(getDatasource().getName(), viewDtos.fromDto(viewDto));

    URI viewUri = UriBuilder.fromUri(uriInfo.getBaseUri().toString()).path(DatasourceResource.class).path(DatasourceResource.class, "getView").build(name, viewDto.getName());
    URI tableUri = UriBuilder.fromUri(uriInfo.getBaseUri().toString()).path(DatasourceResource.class).path(DatasourceResource.class, "getTable").build(name, viewDto.getName());

    return Response.created(viewUri).header("X-Alt-Location", tableUri).build();
  }

  @Path("/view/{viewName}")
  public ViewResource getView(@PathParam("viewName") String viewName) {
    View view = opalRuntime.getViewManager().getView(getDatasource().getName(), viewName);
    return getViewResource(view);
  }

  @GET
  @Path("/locales")
  public Iterable<LocaleDto> getLocales(@QueryParam("locale") String displayLocale) {
    List<LocaleDto> localeDtos = new ArrayList<LocaleDto>();
    for(Locale locale : getLocales()) {
      localeDtos.add(Dtos.asDto(locale, displayLocale != null ? new Locale(displayLocale) : null));
    }

    return localeDtos;
  }

  Datasource getDatasource() {
    Datasource ds = null;
    if(MagmaEngine.get().hasDatasource(name)) {
      ds = MagmaEngine.get().getDatasource(name);
    } else {
      ds = MagmaEngine.get().getTransientDatasourceInstance(name);
      transientDatasourceInstance = ds;
    }
    return ds;
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
