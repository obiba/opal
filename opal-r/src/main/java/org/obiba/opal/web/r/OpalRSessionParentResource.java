/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.DeprecatedOperationException;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 *
 */
@Component
@Transactional
@Scope("request")
@Path("/r/session")
@Tag(name = "R", description = "Operations on R")
public class OpalRSessionParentResource {

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private ApplicationContext applicationContext;

  @Path("/{id}")
  @Operation(
    summary = "Get R session resource",
    description = "Returns a resource handler for a specific R session identified by its ID."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R session resource"),
    @ApiResponse(responseCode = "404", description = "R session not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public RSessionResource getOpalRSessionResource(@PathParam("id") String id) {
    RSessionResource resource = applicationContext.getBean("opalRSessionResource", RSessionResource.class);
    resource.setRServerSession(opalRSessionManager.getSubjectRSession(id));
    return resource;
  }

  @Path("/current")
  @Operation(
    summary = "Get current R session resource",
    description = "Returns a resource handler for the current R session. This operation is deprecated."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "410", description = "Operation deprecated - please upgrade the opal R package"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public RSessionResource getCurrentOpalRSessionResource() {
    throw new DeprecatedOperationException("Unsupported operation: please upgrade your opal R package.");
  }

}
