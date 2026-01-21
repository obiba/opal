/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.core.DeprecatedOperationException;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.web.r.RSessionsResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Component
@Transactional
@Path("/datashield")
@Tag(name = "DataSHIELD", description = "Operations on DataSHIELD")
public class DataShieldResource {

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private ApplicationContext applicationContext;

  @Path("/sessions")
  @Operation(
    summary = "Get DataSHIELD sessions",
    description = "Provides access to DataSHIELD R sessions management."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully accessed sessions resource"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public RSessionsResource getSessions() {
    RSessionsResource resource = applicationContext
        .getBean("datashieldSessionsResource", RSessionsResource.class);
    return resource;
  }

  @Path("/session/{id}")
  @Operation(
    summary = "Get DataSHIELD session",
    description = "Provides access to a specific DataSHIELD R session by ID for session management and operations."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully accessed session resource"),
    @ApiResponse(responseCode = "404", description = "Session not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public DataShieldSessionResource getSession(@PathParam("id") String id) {
    DataShieldSessionResource resource = applicationContext
        .getBean("dataShieldSessionResource", DataShieldSessionResource.class);
    resource.setRServerSession(opalRSessionManager.getSubjectRSession(id));
    return resource;
  }

  @Path("/session/current")
  @Operation(
    summary = "Get current DataSHIELD session (deprecated)",
    description = "Provides access to the current DataSHIELD R session. This operation is deprecated and no longer supported."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "501", description = "Operation deprecated - please upgrade opal R package"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public DataShieldSessionResource getCurrentSession() {
    throw new DeprecatedOperationException("Unsupported operation: please upgrade your opal R package.");
  }

  @Path("/env/{name}")
  @Operation(
    summary = "Get DataSHIELD environment",
    description = "Provides access to DataSHIELD environment resources (AGGREGATE, ASSIGN) for method management and operations."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully accessed environment resource"),
    @ApiResponse(responseCode = "400", description = "Invalid environment name"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public DataShieldEnvironmentResource getEnvironment(@PathParam("name") String env) {
    DataShieldEnvironmentResource resource = applicationContext.getBean(DataShieldEnvironmentResource.class);
    resource.setMethodType(DSMethodType.valueOf(env.toUpperCase()));
    return resource;
  }

}
