package org.obiba.opal.web.magma.math;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface CategoricalSummaryResource extends SummaryResource {

  @GET
  @POST
  Response get(@QueryParam("distinct") boolean distinct, @QueryParam("offset") Integer offset,
      @QueryParam("limit") Integer limit);
}
