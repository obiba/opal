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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.DatasourceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Component
@Transactional
@Path("/datasources")
@Api(value = "/datasources", description = "Operations about datasources")
public class DatasourcesResource {

  @Autowired
  private ApplicationContext applicationContext;

  @GET
  @ApiOperation(value = "Get all datasources",
      notes = "The Identifiers datasource will not be returned here", response = List.class)
  public List<Magma.DatasourceDto> getDatasources() {
    List<Magma.DatasourceDto> datasources = Lists.newArrayList();
    for(Datasource from : MagmaEngine.get().getDatasources()) {
      URI dsLink = UriBuilder.fromPath("/").path(DatasourceResource.class).build(from.getName());
      Magma.DatasourceDto.Builder ds = Dtos.asDto(from).setLink(dsLink.toString());
      datasources.add(ds.build());
    }
    sortByName(datasources);
    return datasources;
  }

  @GET
  @Path("/tables")
  @ApiOperation(value = "Get all tables of all datasources",
      notes = "The Identifiers datasource will not be returned here", response = List.class)
  public List<Magma.TableDto> getTables(@Nullable @QueryParam("entityType") String entityType) {
    List<Magma.TableDto> tables = Lists.newArrayList();
    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      tables.addAll(getDatasourceTablesResource(datasource).getTables(false, entityType));
    }
    return tables;
  }

  @GET
  @Path("/count")
  @ApiOperation(value = "Get the number of datasources", response = Integer.class)
  public Response getDatasourcesCount() {
    return Response.ok().entity(String.valueOf(MagmaEngine.get().getDatasources().size())).build();

  }

  private DatasourceTablesResource getDatasourceTablesResource(Datasource datasource) {
    DatasourceTablesResource datasourceTablesResource = applicationContext.getBean(DatasourceTablesResource.class);
    datasourceTablesResource.setDatasource(datasource);
    return datasourceTablesResource;
  }

  private void sortByName(List<Magma.DatasourceDto> datasources) {
    // sort alphabetically
    Collections.sort(datasources, new Comparator<Magma.DatasourceDto>() {

      @Override
      public int compare(DatasourceDto d1, DatasourceDto d2) {
        return d1.getName().compareTo(d2.getName());
      }

    });
  }

}
