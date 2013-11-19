package org.obiba.opal.web.datashield;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.web.model.DataShield;

public interface DataShieldEnvironmentResource {

  void setEnvironment(DatashieldConfiguration.Environment environment);

  @GET
  @Path("/methods")
  List<DataShield.DataShieldMethodDto> getDataShieldMethods();

  @DELETE
  @Path("/methods")
  Response deleteDataShieldMethods();

  @POST
  @Path("/methods")
  Response createDataShieldMethod(@Context UriInfo uri, DataShield.DataShieldMethodDto dto);

  @GET
  @Path("/method/{name}")
  Response getDataShieldMethod(@PathParam("name") String name);

  @PUT
  @Path("/method/{name}")
  Response updateDataShieldMethod(@PathParam("name") String name, DataShield.DataShieldMethodDto dto);

  @DELETE
  @Path("/method/{name}")
  Response deleteDataShieldMethod(@PathParam("name") String name);
}
