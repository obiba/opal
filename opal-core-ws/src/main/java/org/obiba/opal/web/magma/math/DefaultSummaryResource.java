package org.obiba.opal.web.magma.math;

import javax.ws.rs.GET;
import javax.ws.rs.POST;

import org.obiba.opal.web.model.Math;

public interface DefaultSummaryResource extends SummaryResource {

  @GET
  @POST
  Math.SummaryStatisticsDto get();

}
