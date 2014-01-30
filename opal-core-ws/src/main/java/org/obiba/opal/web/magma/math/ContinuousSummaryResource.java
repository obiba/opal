package org.obiba.opal.web.magma.math;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.magma.math.ContinuousVariableSummary;

public interface ContinuousSummaryResource extends SummaryResource {
  @GET
  @POST
  Response get(@QueryParam("d") @DefaultValue("normal") ContinuousVariableSummary.Distribution distribution,
      @QueryParam("p") List<Double> percentiles, @QueryParam("intervals") @DefaultValue("10") int intervals,
      @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit,
      @QueryParam("resetCache") Boolean resetCache);
}
