package org.obiba.opal.web.magma.math;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface BinarySummaryResource extends SummaryResource {

  @GET
  Response get(@QueryParam("offset") Integer offset, //
      @QueryParam("limit") Integer limit, //
      @QueryParam("fullIfCached") @DefaultValue("false") boolean fullIfCached, //
      @QueryParam("resetCache") @DefaultValue("false") boolean resetCache);

}
