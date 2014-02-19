package org.obiba.opal.web.magma.math;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface DefaultSummaryResource extends SummaryResource {

  @GET
  Response get(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit,
      @QueryParam("resetCache") @DefaultValue("false") boolean resetCache);

  @GET
  @Path("/cached-full-or-limit")
  Response getCachedFullOrComputeLimit(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit);
}
