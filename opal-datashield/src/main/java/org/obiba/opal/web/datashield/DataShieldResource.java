/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.datashield;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.datashield.DataShieldMethod;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.datashield.support.DataShieldMethodConverterRegistry;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.r.OpalRSessionResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/datashield")
public class DataShieldResource {

  private final OpalRService opalRService;

  private final OpalRuntime opalRuntime;

  private final OpalRSessionManager opalRSessionManager;

  private final DataShieldMethodConverterRegistry methodConverterRegistry;

  @Autowired
  public DataShieldResource(OpalRService opalRService, OpalRuntime opalRuntime, OpalRSessionManager opalRSessionManager, DataShieldMethodConverterRegistry methodConverterRegistry) {
    if(opalRService == null) throw new IllegalArgumentException("opalRService cannot be null");
    if(opalRuntime == null) throw new IllegalArgumentException("opalRuntime cannot be null");
    if(opalRSessionManager == null) throw new IllegalArgumentException("opalRSessionManager cannot be null");
    if(methodConverterRegistry == null) throw new IllegalArgumentException("methodConverterRegistry cannot be null");
    this.opalRService = opalRService;
    this.opalRuntime = opalRuntime;
    this.opalRSessionManager = opalRSessionManager;
    this.methodConverterRegistry = methodConverterRegistry;
  }

  @Path("/session/{id}")
  public OpalRSessionResource getSession(@PathParam("id") String id) {
    return new OpalDataShieldSessionResource(opalRService, opalRuntime, opalRSessionManager, opalRSessionManager.getSubjectRSession(id));
  }

  @Path("/session/current")
  public OpalRSessionResource getCurrentSession() {
    if(opalRSessionManager.hasSubjectCurrentRSession() == false) {
      opalRSessionManager.newSubjectCurrentRSession();
    }
    return new OpalDataShieldSessionResource(opalRService, opalRuntime, opalRSessionManager, opalRSessionManager.getSubjectCurrentRSession());
  }

  @GET
  @Path("/methods")
  public List<DataShield.DataShieldMethodDto> getDataShieldMethods() {
    final List<DataShield.DataShieldMethodDto> dtos = Lists.newArrayList();
    for(DataShieldMethod method : listMethods()) {
      dtos.add(methodConverterRegistry.asDto(method));
    }
    sortByName(dtos);
    return dtos;
  }

  @POST
  @Path("/methods")
  public Response createDataShieldMethod(DataShield.DataShieldMethodDto dto) {
    DatashieldConfiguration config = getDatashieldConfiguration();
    if(config.hasDataShieldMethod(dto.getName())) return Response.status(Status.BAD_REQUEST).build();

    config.addAggregatingMethod(methodConverterRegistry.parse(dto));
    opalRuntime.writeOpalConfiguration();
    UriBuilder ub = UriBuilder.fromPath("/").path(DataShieldResource.class).path(DataShieldResource.class, "getDataShieldMethod");
    return Response.created(ub.build(dto.getName())).build();
  }

  @GET
  @Path("/method/{name}")
  public Response getDataShieldMethod(@PathParam("name") String name) {
    return Response.ok().entity(methodConverterRegistry.asDto(getDatashieldConfiguration().getDataShieldMethod(name))).build();
  }

  @PUT
  @Path("/method/{name}")
  public Response updateDataShieldMethod(@PathParam("name") String name, DataShield.DataShieldMethodDto dto) {
    if(!name.equals(dto.getName())) return Response.status(Status.BAD_REQUEST).build();

    DatashieldConfiguration config = getDatashieldConfiguration();
    if(!config.hasDataShieldMethod(name)) return Response.status(Status.NOT_FOUND).build();

    config.addAggregatingMethod(methodConverterRegistry.parse(dto));
    opalRuntime.writeOpalConfiguration();

    return Response.ok().build();
  }

  @DELETE
  @Path("/method/{name}")
  public Response deleteDataShieldMethod(@PathParam("name") String name) {
    getDatashieldConfiguration().removeAggregatingMethod(name);
    opalRuntime.writeOpalConfiguration();
    return Response.ok().build();
  }

  private Iterable<DataShieldMethod> listMethods() {
    return getDatashieldConfiguration().getAggregatingMethods();
  }

  private DatashieldConfiguration getDatashieldConfiguration() {
    OpalConfiguration cfg = opalRuntime.getOpalConfiguration();
    return cfg.getExtension(DatashieldConfiguration.class);
  }

  private void sortByName(List<DataShield.DataShieldMethodDto> dtos) {
    // sort alphabetically
    Collections.sort(dtos, new Comparator<DataShield.DataShieldMethodDto>() {

      @Override
      public int compare(DataShield.DataShieldMethodDto d1, DataShield.DataShieldMethodDto d2) {
        return d1.getName().compareTo(d2.getName());
      }

    });
  }

}
