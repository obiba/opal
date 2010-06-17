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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/datasource/{name}")
public class DatasourceResource {

  private static final Logger log = LoggerFactory.getLogger(DatasourceResource.class);

  @PathParam("name")
  private String name;

  // Used by Spring. JAX-RS, will inject the name attribute
  public DatasourceResource() {
  }

  // Used for testing
  public DatasourceResource(String name) {
    this.name = name;
  }

  @GET
  public Magma.DatasourceDto get() {
    Datasource ds = MagmaEngine.get().getDatasource(name);
    Magma.DatasourceDto.Builder datasource = Magma.DatasourceDto.newBuilder().setName(ds.getName());
    for(ValueTable table : ds.getValueTables()) {
      datasource.addTable(table.getName());
    }
    return datasource.build();
  }

  @GET
  @Path("/dictionary/excel")
  @NotAuthenticated
  public Response getExcelDictionary() throws MagmaRuntimeException, IOException {
    String destinationName = name + "-dictionary";
    ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
    ExcelDatasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);

    MagmaEngine.get().addDatasource(destinationDatasource);
    DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();
    copier.copy(MagmaEngine.get().getDatasource(name), destinationDatasource);
    MagmaEngine.get().removeDatasource(destinationDatasource);

    return Response.ok(excelOutput.toByteArray(), "application/vnd.ms-excel").header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
  }

  @Path("/table/{table}")
  public TableResource getTable(@PathParam("table") String table) {
    return getTableResource(MagmaEngine.get().getDatasource(name).getValueTable(table));
  }

  @Path("/tables")
  public TablesResource getTables() {
    return new TablesResource(name);
  }

  @Bean
  @Scope("request")
  public TableResource getTableResource(ValueTable table) {
    return new TableResource(table);
  }

}
