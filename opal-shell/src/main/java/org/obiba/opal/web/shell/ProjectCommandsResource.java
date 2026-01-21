/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.shell;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.web.BaseResource;
import org.obiba.opal.web.model.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("request")
@Path("/project/{name}/commands")
@Tag(name = "Projects", description = "Operations on projects")
@Tag(name = "Tasks", description = "Operations on tasks")
@SuppressWarnings("OverlyCoupledClass")
public class ProjectCommandsResource implements BaseResource {

  private static final Logger log = LoggerFactory.getLogger(ProjectCommandsResource.class);

  @PathParam("name")
  protected String name;

  @Autowired
  private CommandJobService commandJobService;

  @GET
  @Operation(
    summary = "Get project commands",
    description = "Retrieves the list of command jobs associated with a specific project."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved project command list"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<Commands.CommandStateDto> getCommands() {
    List<Commands.CommandStateDto> commandDtoList = new ArrayList<>();

    List<CommandJob> history = commandJobService.getHistory();
    for(CommandJob commandJob : history) {
      if(commandJob.hasProject() && commandJob.getProject().equals(name)) {
        commandDtoList.add(Dtos.asDto(commandJob));
      }
    }

    return commandDtoList;
  }
}
