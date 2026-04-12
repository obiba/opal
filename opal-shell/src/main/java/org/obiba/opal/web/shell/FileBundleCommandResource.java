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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.FileBundleCommandOptions;
import org.obiba.opal.shell.web.FileBundleCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/shell/commands/_file-bundle")
@Tag(name = "Tasks", description = "Operations on tasks")
public class FileBundleCommandResource extends AbstractCommandsResource {

  @Autowired
  @Qualifier("web")
  private CommandRegistry commandRegistry;

  @POST
  @Operation(
      summary = "Create file bundle",
      description = "Bundles a file or folder as a zip archive in the work directory, optionally password-protected."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "File bundle command successfully launched"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @NoAuthorization
  public Response createFileBundle(Commands.FileBundleCommandOptionsDto options) {
    // No authorization since every user has a home folder
    // bundle operation will filter only readable files
    Command<FileBundleCommandOptions> cmd = commandRegistry.newCommand("file-bundle");
    cmd.setOptions(new FileBundleCommandOptionsDtoImpl(options));
    return launchCommand(cmd);
  }

  @Override
  protected Response buildLaunchCommandResponse(CommandJob commandJob) {
    return Response.created(
            UriBuilder.fromPath("/").path(WebShellResource.class).path(WebShellResource.class, "getCommand")
                .build(commandJob.getId()))
        .entity(Dtos.asDto(commandJob))
        .build();
  }
}

