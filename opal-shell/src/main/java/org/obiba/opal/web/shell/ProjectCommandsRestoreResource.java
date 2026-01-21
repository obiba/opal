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
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.RestoreCommandOptions;
import org.obiba.opal.shell.web.RestoreCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.support.ConflictingRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/project/{name}/commands/_restore")
@Tag(name = "Projects", description = "Operations on projects")
@Tag(name = "Tasks", description = "Operations on tasks")
public class ProjectCommandsRestoreResource extends AbstractProjectCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(ProjectCommandsRestoreResource.class);

  @PathParam("name")
  protected String name;

  @Override
  public String getName() {
    return name;
  }

  @POST
  @Operation(
    summary = "Restore project from backup",
    description = "Restores a project from a backup archive. The project must not be currently busy with other operations."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Restore command successfully launched"),
    @ApiResponse(responseCode = "409", description = "Project is momentarily not reloadable"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access backup archive"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response restoreProject(Commands.RestoreCommandOptionsDto options) {
    if (checkCommandIsBlocked(name, false)) throw new ConflictingRequestException("ProjectMomentarilyNotReloadable", name);

    String commandName = "restore";
    ensureFileReadAccess(options.getArchive());
    Command<RestoreCommandOptions> restoreCommand = commandRegistry.newCommand(commandName);
    restoreCommand.setOptions(new RestoreCommandOptionsDtoImpl(name, options));

    return launchCommand(commandName, restoreCommand);
  }
}
