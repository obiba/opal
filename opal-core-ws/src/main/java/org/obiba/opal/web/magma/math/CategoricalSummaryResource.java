package org.obiba.opal.web.magma.math;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface CategoricalSummaryResource extends SummaryResource {

  @GET
  @POST // requires POST since the request body contains variable info (categories, script, etc)
  Response get(@QueryParam("distinct") boolean distinct, //
      @QueryParam("offset") Integer offset, //
      @QueryParam("limit") Integer limit, //
      @QueryParam("fullIfCached") @DefaultValue("false") boolean fullIfCached, //
      @QueryParam("resetCache") @DefaultValue("false") boolean resetCache);

}
