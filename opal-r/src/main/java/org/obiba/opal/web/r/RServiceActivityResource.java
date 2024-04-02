/*
 * Copyright (c) 2023 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import com.google.common.base.Strings;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.r.service.RActivityService;
import org.obiba.opal.r.service.RActivitySummary;
import org.obiba.opal.r.service.RSessionActivity;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/service/r/activity")
public class RServiceActivityResource {

  private final RActivityService rActivityService;

  @Autowired
  public RServiceActivityResource(RActivityService rActivityService) {
    this.rActivityService = rActivityService;
  }

  @GET
  public List<OpalR.RSessionActivityDto> getActivities(@QueryParam("context") String context,
                                                       @QueryParam("user") String user, @QueryParam("profile") String profile,
                                                       @QueryParam("from") String from, @QueryParam("to") String to) {
    Date fromDate = asDate(from);
    Date toDate = asDate(to);
    if (Strings.isNullOrEmpty(context)) throw new BadRequestException("R context is missing");
    return asDto(rActivityService.getActivities(context, user, profile, fromDate, toDate));
  }

  @GET
  @Path("_summary")
  public List<OpalR.RActivitySummaryDto> getActivitySummaries(@QueryParam("context") String context,
                                                              @QueryParam("user") String user, @QueryParam("profile") String profile,
                                                              @QueryParam("from") String from, @QueryParam("to") String to) {
    Date fromDate = asDate(from);
    Date toDate = asDate(to);
    if (Strings.isNullOrEmpty(context)) throw new BadRequestException("R context is missing");
    return asSummaryDto(rActivityService.getActivitySummaries(context, user, profile, fromDate, toDate));
  }

  private Date asDate(String dateStr) {
    Value value = DateTimeType.get().valueOf(dateStr);
    return value.isNull() ? null : (Date) value.getValue();
  }

  private List<OpalR.RSessionActivityDto> asDto(List<RSessionActivity> activities) {
    return activities.stream()
        .map(activity -> OpalR.RSessionActivityDto.newBuilder()
            .setContext(activity.getContext())
            .setUser(activity.getUser())
            .setProfile(activity.getProfile())
            .setExecutionTimeMillis(activity.getExecutionTimeMillis())
            .setCreatedDate(DateTimeType.get().valueOf(activity.getCreated()).toString())
            .setUpdatedDate(DateTimeType.get().valueOf(activity.getUpdated()).toString())
            .build())
        .collect(Collectors.toList());
  }

  private List<OpalR.RActivitySummaryDto> asSummaryDto(List<RActivitySummary> summaries) {
    return summaries.stream()
        .filter(summary -> summary.getSessionsCount() > 0)
        .map(summary -> OpalR.RActivitySummaryDto.newBuilder()
            .setContext(summary.getContext())
            .setUser(summary.getUser())
            .setProfile(summary.getProfile())
            .setExecutionTimeMillis(summary.getExecutionTimeMillis())
            .setStartDate(DateTimeType.get().valueOf(summary.getCreated()).toString())
            .setEndDate(DateTimeType.get().valueOf(summary.getUpdated()).toString())
            .setSessionsCount(summary.getSessionsCount())
            .build())
        .collect(Collectors.toList());
  }
}
