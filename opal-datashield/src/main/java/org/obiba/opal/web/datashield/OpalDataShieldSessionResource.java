package org.obiba.opal.web.datashield;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.r.OpalRSessionResource;

public interface OpalDataShieldSessionResource extends OpalRSessionResource {

  @POST
  @Path("/aggregate")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response aggregate(@QueryParam("async") @DefaultValue("false") boolean async, String body);

}
