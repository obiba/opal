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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.DuplicateDatasourceNameException;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.magma.support.NoSuchDatasourceFactoryException;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.DatasourceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/datasources")
public class DatasourcesResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DatasourcesResource.class);

  private final DatasourceFactoryRegistry datasourceFactoryRegistry;

  private final OpalConfigurationService configService;

  @Autowired
  public DatasourcesResource(DatasourceFactoryRegistry datasourceFactoryRegistry, OpalConfigurationService configService) {
    if(datasourceFactoryRegistry == null) throw new IllegalArgumentException("datasourceFactoryRegistry cannot be null");
    if(configService == null) throw new IllegalArgumentException("configService cannot be null");

    this.configService = configService;
    this.datasourceFactoryRegistry = datasourceFactoryRegistry;
  }

  @GET
  public List<Magma.DatasourceDto> getDatasources() {
    final List<Magma.DatasourceDto> datasources = Lists.newArrayList();

    for(Datasource from : MagmaEngine.get().getDatasources()) {
      URI dslink = UriBuilder.fromPath("/").path(DatasourceResource.class).build(from.getName());
      Magma.DatasourceDto.Builder ds = Dtos.asDto(from).setLink(dslink.toString());
      datasources.add(ds.build());
    }
    sortByName(datasources);

    return datasources;
  }

  @POST
  public Response createDatasource(@Context final UriInfo uriInfo, Magma.DatasourceFactoryDto factoryDto) {
    ResponseBuilder response = null;
    try {
      DatasourceFactory factory = datasourceFactoryRegistry.parse(factoryDto);
      Datasource ds = MagmaEngine.get().addDatasource(factory);
      configService.getOpalConfiguration().getMagmaEngineFactory().withFactory(factory);
      configService.writeOpalConfiguration();
      UriBuilder ub = uriInfo.getBaseUriBuilder().path("datasource").path(ds.getName());
      response = Response.created(ub.build()).entity(Dtos.asDto(ds).build());
    } catch(NoSuchDatasourceFactoryException noSuchDatasourceFactoryEx) {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "UnidentifiedDatasourceFactory").build());
    } catch(DuplicateDatasourceNameException duplicateDsNameEx) {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "DuplicateDatasourceName").build());
    } catch(DatasourceParsingException dsParsingEx) {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "DatasourceCreationFailed", dsParsingEx).build());
    } catch(MagmaRuntimeException dsCreationFailedEx) {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "DatasourceCreationFailed", dsCreationFailedEx).build());
    }

    return response.build();
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
