package org.obiba.opal.web.r;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.r.service.OpalRSession;

public interface RSymbolResource {

  void setName(String name);

  void setOpalRSession(OpalRSession rSession);

  void setIdentifiersTableService(IdentifiersTableService identifiersTableService);

  String getName();

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response getSymbol();

  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  Response putString(@Context UriInfo uri, String content);

  @PUT
  @Consumes("application/x-rscript")
  Response putRScript(@Context UriInfo uri, String script);

  @PUT
  @Consumes("application/x-opal")
  Response putMagma(@Context UriInfo uri, String path, @QueryParam("variables") String variableFilter,
      @QueryParam("missings") @DefaultValue("false") Boolean missings, @QueryParam("identifiers") String identifiers);

  @DELETE
  Response rm();
}
