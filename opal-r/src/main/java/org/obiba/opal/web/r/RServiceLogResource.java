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
import org.obiba.opal.r.service.RServerManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.PrintWriter;

@Component
@Scope("request")
@Path("/service/r/log")
@Tag(name = "R", description = "Operations on R")
public class RServiceLogResource {

  private static final Logger log = LoggerFactory.getLogger(RServiceLogResource.class);

  @Autowired
  protected RServerManagerService rServerManagerService;

  @GET
  @Path("Rserve.log")
  @Operation(
    summary = "Get R server log",
    description = "Retrieves the R server log file content, optionally limited to a specific number of lines from the end of the file."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R server log"),
    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
    @ApiResponse(responseCode = "404", description = "R server or profile not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response tailRserveLog(@QueryParam("n") @DefaultValue("10000") Integer nbLines, @QueryParam("profile") String profile) {
    String[] rlog = rServerManagerService.getRServer(profile).getLog(nbLines);
    log.info("received {} lines", rlog.length);
    StreamingOutput stream = output -> {
      try (PrintWriter writer = new PrintWriter(output)) {
        for (String line : rlog) {
          writer.println(line);
        }
      }
    };
    return Response.ok(stream, "text/plain")
        .header("Content-Disposition", "attachment; filename=Rserve.log").build();
  }

}
