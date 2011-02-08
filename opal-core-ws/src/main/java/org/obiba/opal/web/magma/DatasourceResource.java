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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

import org.obiba.core.util.StreamUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.views.View;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.model.Opal.LocaleDto;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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

  @PUT
  @Path("/table/{table}")
  public Response createTable(@Context UriInfo uri, @PathParam("table") String table, List<Variable> variables) throws IOException {
    Datasource ds = getDatasource();
    if(ds.hasValueTable(table)) {
      throw new IllegalStateException("");
    }

    ValueTableWriter writer = ds.createWriter(table, variables.iterator().next().getEntityType());
    try {
      VariableWriter vw = writer.writeVariables();
      for(Variable v : variables) {
        vw.writeVariable(v);
      }
      vw.close();
    } finally {
      writer.close();
    }
    return Response.created(uri.getAbsolutePath()).build();
  }

  @Path("/tables")
  public TablesResource getTables() {
    return new TablesResource(getDatasource());
  }

  @Bean
  @Scope("request")
  public TableResource getTableResource(ValueTable table) {
    return new TableResource(table, getLocales());
  }

  @Bean
  @Scope("request")
  public ViewResource getViewResource(View view) {
    return new ViewResource(view, viewDtos, getLocales());
  }

  @POST
  @Path("/tables")
  public Response createTable(TableDto table) {

    try {

      Datasource datasource = MagmaEngine.get().getDatasource(name);

      // ClientErrorDto errorDto;

      // @TODO Verify that the datasource allows table creation (Magma does not offer this yet)
      // if(datasource.isReadOnly()) {
      // errorMessage = return Response.status(Status.BAD_REQUEST).entity(getErrorMessage(Status.BAD_REQUEST,
      // "CannotCreateTable")).build();
      // } else

      if(datasource.hasValueTable(table.getName())) {
        return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "TableAlreadyExists").build()).build();
      } else {
        writeVariablesToTable(table, datasource);
        return Response.created(UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getTable").build(name, table.getName())).build();
      }
    } catch(Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ClientErrorDtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, e.getMessage()).build()).build();
    }
  }

  @Path("/compare")
  @Bean
  @Scope("request")
  public CompareResource getTableCompare() {
    return new CompareResource(getDatasource());
  }

  @PUT
  @Path("/view/{viewName}")
  public Response createOrUpdateView(@PathParam("viewName") String viewName, ViewDto viewDto) {
    if(datasourceHasTable(viewName) && !datasourceHasView(viewName)) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "TableAlreadyExists").build()).build();
    }
    opalRuntime.getViewManager().addView(getDatasource().getName(), viewDtos.fromDto(viewName, viewDto));

    return Response.ok().build();
  }

  @DELETE
  @Path("/view/{viewName}")
  public Response removeView(@PathParam("viewName") String viewName) {
    if(!datasourceHasView(viewName)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    opalRuntime.getViewManager().removeView(getDatasource().getName(), viewName);

    return Response.ok().build();
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

  private void writeVariablesToTable(TableDto table, Datasource datasource) {
    VariableWriter vw = null;
    try {
      vw = datasource.createWriter(table.getName(), table.getEntityType()).writeVariables();

      for(VariableDto dto : table.getVariablesList()) {
        vw.writeVariable(Dtos.fromDto(dto));
      }
    } finally {
      StreamUtil.silentSafeClose(vw);
    }
  }

  private boolean datasourceHasTable(String viewName) {
    return getDatasource().hasValueTable(viewName);
  }

  private boolean datasourceHasView(String viewName) {
    return opalRuntime.getViewManager().hasView(getDatasource().getName(), viewName);
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
