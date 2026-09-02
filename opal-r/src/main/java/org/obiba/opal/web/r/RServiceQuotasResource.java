/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.shiro.SecurityUtils;
import org.obiba.opal.r.service.RQuota;
import org.obiba.opal.r.service.RQuotaService;
import org.obiba.opal.r.service.RQuotaUsage;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * The R usage quotas, and what has been consumed against them.
 */
@Component
@Scope("request")
@Path("/service/r/quotas")
@Tag(name = "R", description = "Operations on R")
@Transactional
public class RServiceQuotasResource {

  private final RQuotaService rQuotaService;

  @Autowired
  public RServiceQuotasResource(RQuotaService rQuotaService) {
    this.rQuotaService = rQuotaService;
  }

  @GET
  @Operation(
      summary = "Get R quotas",
      description = "Retrieves the R usage quotas, optionally restricted to an execution context."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved R quotas", useReturnTypeSchema = true)
  })
  public List<OpalR.RQuotaDto> getQuotas(@QueryParam("context") String context) {
    return rQuotaService.getQuotas(context).stream()
        .sorted(Comparator.comparing(RQuota::getContext)
            .thenComparing(RQuota::getSubjectType)
            .thenComparing(RQuota::getPrincipal)
            .thenComparing(RQuota::getMetric))
        .map(Dtos::asDto)
        .toList();
  }

  @POST
  @Operation(
      summary = "Create a R quota",
      description = "Creates a R usage quota for a subject: the system default, a group or a user. A subject can have one quota per metric."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "R quota created"),
      @ApiResponse(responseCode = "400", description = "Invalid quota")
  })
  public Response createQuota(@Context UriInfo info, OpalR.RQuotaDto dto) {
    RQuota quota = rQuotaService.saveQuota(Dtos.fromDto(dto));
    return Response.created(info.getBaseUriBuilder().path("service").path("r").path("quota")
            .path(String.valueOf(quota.getId())).build())
        .entity(Dtos.asDto(quota)).build();
  }

  @GET
  @Path("_usage")
  @Operation(
      summary = "Get the R quota usage of a user",
      description = "Retrieves, for each usage metric, the quota that applies to a user in an execution context and what they have consumed against it."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the R quota usage", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "Missing required context or user parameter")
  })
  public List<OpalR.RQuotaUsageDto> getUsage(@QueryParam("context") String context, @QueryParam("user") String user) {
    if (Strings.isNullOrEmpty(context)) throw new BadRequestException("R context is missing");
    if (Strings.isNullOrEmpty(user)) throw new BadRequestException("User is missing");
    return asDto(rQuotaService.getUsages(context, user));
  }

  @GET
  @Path("_current")
  @NoAuthorization
  @Operation(
      summary = "Get the R quota usage of the current user",
      description = "Retrieves, for each usage metric, the quota that applies to the authenticated user in an execution context and what they have consumed against it."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the R quota usage", useReturnTypeSchema = true),
      @ApiResponse(responseCode = "400", description = "Missing required context parameter")
  })
  public List<OpalR.RQuotaUsageDto> getCurrentUsage(@QueryParam("context") String context) {
    if (Strings.isNullOrEmpty(context)) throw new BadRequestException("R context is missing");
    return asDto(rQuotaService.getUsages(context, getPrincipal()));
  }

  /**
   * Every metric is reported, whether or not a quota applies to it, so that a client never has to know how many
   * metrics exist to render the answer: an entry without a quota is the unlimited case.
   */
  private List<OpalR.RQuotaUsageDto> asDto(List<RQuotaUsage> usages) {
    return usages.stream().map(Dtos::asDto).toList();
  }

  /**
   * The subject of the request, which is the only user {@code _current} will report on: it takes no {@code user}
   * parameter, so having no authorization check on it cannot be turned into reading someone else's usage.
   */
  private String getPrincipal() {
    Object principal = SecurityUtils.getSubject().getPrincipal();
    if (principal == null) throw new ForbiddenException("Not authenticated");
    return principal.toString();
  }
}
