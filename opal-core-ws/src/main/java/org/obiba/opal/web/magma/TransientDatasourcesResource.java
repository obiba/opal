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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.magma.support.NoSuchDatasourceFactoryException;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Transactional
@Path("/transient-datasources")
public class TransientDatasourcesResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TransientDatasourcesResource.class);

  private DatasourceFactoryRegistry datasourceFactoryRegistry;

  @Autowired
  public void setDatasourceFactoryRegistry(DatasourceFactoryRegistry datasourceFactoryRegistry) {
    this.datasourceFactoryRegistry = datasourceFactoryRegistry;
  }

  @POST
  @NoAuthorization
  public Response createDatasource(@Context UriInfo uriInfo, Magma.DatasourceFactoryDto factoryDto) {
    String uid = null;
    ResponseBuilder response = null;
    try {
      DatasourceFactory factory = datasourceFactoryRegistry.parse(factoryDto);
      uid = MagmaEngine.get().addTransientDatasource(factory);
      Datasource ds = MagmaEngine.get().getTransientDatasourceInstance(uid);
      response = Response.created(UriBuilder.fromPath("/").path(DatasourceResource.class).build(uid))
          .entity(Dtos.asDto(ds).build());
    } catch(NoSuchDatasourceFactoryException e) {
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "UnidentifiedDatasourceFactory").build());
    } catch(DatasourceParsingException pe) {
      MagmaEngine.get().removeTransientDatasource(uid);
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DatasourceCreationFailed", pe));
    } catch(MagmaRuntimeException e) {
      MagmaEngine.get().removeTransientDatasource(uid);
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DatasourceCreationFailed", e));
    }

    return response.build();
  }

}
