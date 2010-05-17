/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Magma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Path("/datasource")
public class DatasourceResource {

  private static final Logger log = LoggerFactory.getLogger(DatasourceResource.class);

  @GET
  @Path("/{name}")
  public Magma.DatasourceDto get(@PathParam("name") String name) {
    Datasource ds = MagmaEngine.get().getDatasource(name);
    Magma.DatasourceDto.Builder datasource = Magma.DatasourceDto.newBuilder().setName(ds.getName());
    for(ValueTable table : ds.getValueTables()) {
      datasource.addTable(table.getName());
    }
    return datasource.build();
  }

  @Path("/{name}/table/{table}")
  public TableResource getTable(@PathParam("name") String name, @PathParam("table") String table) {
    return getTableResource(MagmaEngine.get().getDatasource(name).getValueTable(table));
  }

  @Bean
  @Scope("request")
  public TableResource getTableResource(ValueTable table) {
    return new TableResource(table);
  }

}
