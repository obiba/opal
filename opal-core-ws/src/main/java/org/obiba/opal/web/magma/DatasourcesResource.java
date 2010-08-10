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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.DatasourceDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/datasources")
public class DatasourcesResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DatasourcesResource.class);

  private String keysDatasourceName;

  @Autowired
  private DatasourceFactoryRegistry datasourceFactoryRegistry;

  @Autowired
  public DatasourcesResource(@Value("${org.obiba.opal.keys.tableReference}") String keysTableReference) {
    keysDatasourceName = MagmaEngineTableResolver.valueOf(keysTableReference).getDatasourceName();
    if(keysDatasourceName == null) {
      throw new IllegalArgumentException("invalid keys table reference");
    }
  }

  @GET
  public List<Magma.DatasourceDto> getDatasources() {
    final List<Magma.DatasourceDto> datasources = Lists.newArrayList();

    for(Datasource from : MagmaEngine.get().getDatasources()) {
      // OPAL-365: Hide the keys datasource.
      if(from.getName().equals(keysDatasourceName)) continue;

      URI dslink = UriBuilder.fromPath("/").path(DatasourceResource.class).build(from.getName());
      Magma.DatasourceDto.Builder ds = Magma.DatasourceDto.newBuilder() //
      .setName(from.getName()) //
      .setLink(dslink.toString());

      final List<String> tableNames = Lists.newArrayList();
      for(ValueTable table : from.getValueTables()) {
        tableNames.add(table.getName());
      }
      Collections.sort(tableNames);
      ds.addAllTable(tableNames);

      datasources.add(ds.build());
    }
    sortByName(datasources);

    return datasources;
  }

  @POST
  public Response createDatasource(@Context final UriInfo uriInfo, Magma.DatasourceFactoryDto factoryDto) {
    DatasourceFactory factory = datasourceFactoryRegistry.parse(factoryDto);

    if(factory != null) {
      String uid = MagmaEngine.get().addTransientDatasource(factory);
      Datasource ds = MagmaEngine.get().getTransientDatasourceInstance(uid);
      UriBuilder ub = uriInfo.getBaseUriBuilder().path("datasource").path(uid);
      return Response.ok().entity(Dtos.asDto(ds).build()).location(ub.build()).build();
    } else {
      return Response.status(Status.BAD_REQUEST).entity(getErrorMessage(Status.BAD_REQUEST, "UnidentifiedDatasourceFactory")).build();
    }
  }

  public void setDatasourceFactoryRegistry(DatasourceFactoryRegistry datasourceFactoryRegistry) {
    this.datasourceFactoryRegistry = datasourceFactoryRegistry;
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

  private ClientErrorDto getErrorMessage(Status responseStatus, String errorStatus) {
    return ClientErrorDto.newBuilder().setCode(responseStatus.getStatusCode()).setStatus(errorStatus).build();
  }

}
