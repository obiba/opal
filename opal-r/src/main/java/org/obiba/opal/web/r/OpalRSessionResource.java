package org.obiba.opal.web.r;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.web.model.OpalR;

public interface OpalRSessionResource {

  void setOpalRSession(OpalRSession rSession);

  @GET
  OpalR.RSessionDto getRSession();

  /**
   * Destroy the R session and optionally save the associated workspace.
   * @param save
   * @return
   */
  @DELETE
  Response removeRSession(@QueryParam("save") boolean save);

  @PUT
  @Path("/current")
  Response setCurrentRSession();

  @POST
  @Path("/execute")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response execute(@QueryParam("script") String script, @QueryParam("async") @DefaultValue("false") boolean async,
      String body);

  @GET
  @Path("/symbols")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response ls();

  @POST
  @Path("/symbols")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  Response assign(MultivaluedMap<String, String> symbols);

  @Path("/symbol/{name}")
  RSymbolResource getRSymbolResource(@PathParam("name") String name);

  @GET
  @Path("/commands")
  List<OpalR.RCommandDto> getRCommands();

  @GET
  @Path("/command/{rid}")
  OpalR.RCommandDto getRCommand(@PathParam("rid") String rid, @QueryParam("wait") @DefaultValue("false") boolean wait);

  @DELETE
  @Path("/command/{rid}")
  Response removeRCommand(@PathParam("rid") String rid);

  @GET
  @Path("/command/{rid}/result")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response getRCommandResult(@PathParam("rid") String rid, @QueryParam("rm") @DefaultValue("true") boolean remove,
      @QueryParam("wait") @DefaultValue("false") boolean wait);
}
