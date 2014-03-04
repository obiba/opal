package org.obiba.opal.web.magma.math;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static org.obiba.magma.math.summary.ContinuousVariableSummary.Distribution;

public interface ContinuousSummaryResource extends SummaryResource {

  @GET
  @POST // requires POST since the request body contains variable info (categories, script, etc)
  Response get(@QueryParam("d") @DefaultValue("normal") Distribution distribution, //
      @QueryParam("p") List<Double> percentiles, //
      @QueryParam("intervals") @DefaultValue("10") int intervals, //
      @QueryParam("offset") Integer offset, //
      @QueryParam("limit") Integer limit, //
      @QueryParam("fullIfCached") @DefaultValue("false") boolean fullIfCached, //
      @QueryParam("resetCache") @DefaultValue("false") boolean resetCache);

}
